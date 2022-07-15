package gaarason.database.eloquent;

import gaarason.database.contract.connection.GaarasonDataSource;
import gaarason.database.contract.eloquent.Builder;
import gaarason.database.contract.eloquent.Model;
import gaarason.database.contract.eloquent.Record;
import gaarason.database.contract.eloquent.RecordList;
import gaarason.database.core.Container;
import gaarason.database.exception.EntityNotFoundException;
import gaarason.database.exception.PrimaryKeyNotFoundException;
import gaarason.database.exception.SQLRuntimeException;
import gaarason.database.lang.Nullable;
import gaarason.database.provider.ModelShadowProvider;
import gaarason.database.support.ModelMember;
import gaarason.database.support.PrimaryKeyMember;
import gaarason.database.util.EntityUtils;
import gaarason.database.util.ObjectUtils;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

/**
 * 数据模型对象
 * @author xt
 */
public abstract class ModelBean<T, K> implements Model<T, K> {

    private static final String DEFAULT_SOFT_DELETED_COLUMN_NAME = "is_deleted";

    private static final String DEFAULT_SOFT_DELETED_VALUE_YSE = "1";

    private static final String DEFAULT_SOFT_DELETED_VALUE_NO = "0";

    /**
     * Model信息大全
     * 注:不需要volatile修饰
     */
    @Nullable
    protected transient ModelShadowProvider modelShadow;

    /**
     * @return dataSource代理
     */
    public abstract GaarasonDataSource getGaarasonDataSource();

    @Override
    public int delete(Builder<T, K> builder) {
        return softDeleting() ? softDelete(builder) : builder.forceDelete();
    }

    @Override
    public int restore(Builder<T, K> builder) {
        return softDeleteRestore(builder);
    }

    /**
     * Model信息
     * @return ModelShadow
     */
    protected ModelShadowProvider getModelShadow() {
        ModelShadowProvider localModelShadow = modelShadow;
        if (localModelShadow == null) {
            synchronized (this) {
                localModelShadow = modelShadow;
                if (localModelShadow == null) {
                    modelShadow = localModelShadow = getGaarasonDataSource().getContainer()
                        .getBean(ModelShadowProvider.class);
                }
            }
        }
        return localModelShadow;
    }

    /**
     * 是否启用软删除
     */
    protected boolean softDeleting() {
        return false;
    }

    /**
     * 软删除查询作用域(反)
     * @param builder 查询构造器
     */
    protected void scopeSoftDeleteOnlyTrashed(Builder<T, K> builder) {
        builder.where(DEFAULT_SOFT_DELETED_COLUMN_NAME, DEFAULT_SOFT_DELETED_VALUE_YSE);
    }

    /**
     * 软删除查询作用域(全)
     * @param builder 查询构造器
     */
    protected void scopeSoftDeleteWithTrashed(Builder<T, K> builder) {

    }

    /**
     * 软删除查询作用域
     * @param builder 查询构造器
     */
    protected void scopeSoftDelete(Builder<T, K> builder) {
        builder.where(DEFAULT_SOFT_DELETED_COLUMN_NAME, DEFAULT_SOFT_DELETED_VALUE_NO);
    }


    /**
     * 软删除实现
     * @param builder 查询构造器
     * @return 删除的行数
     */
    protected int softDelete(Builder<T, K> builder) {
        return builder.data(DEFAULT_SOFT_DELETED_COLUMN_NAME, DEFAULT_SOFT_DELETED_VALUE_YSE).update();
    }

    /**
     * 恢复软删除实现
     * @param builder 查询构造器
     * @return 恢复的行数
     */
    protected int softDeleteRestore(Builder<T, K> builder) {
        return builder.data(DEFAULT_SOFT_DELETED_COLUMN_NAME, DEFAULT_SOFT_DELETED_VALUE_NO).update();
    }

    /**
     * 全局查询作用域
     * @param builder 查询构造器
     * @return 查询构造器
     */
    protected Builder<T, K> apply(Builder<T, K> builder) {
        return builder;
    }

    /**
     * 原始查询构造器
     * @return 原始查询构造器
     */
    protected Builder<T, K> theBuilder() {
        GaarasonDataSource gaarasonDataSource = getGaarasonDataSource();
        return apply(gaarasonDataSource.getQueryBuilder().newBuilder(gaarasonDataSource, this));
    }

    @Override
    public Builder<T, K> newQuery() {
        Builder<T, K> builder = theBuilder();
        if (softDeleting()) {
            scopeSoftDelete(builder);
        }
        return builder;
    }

    @Override
    public Builder<T, K> withTrashed() {
        Builder<T, K> builder = theBuilder();
        scopeSoftDeleteWithTrashed(builder);
        return builder;
    }

    @Override
    public Builder<T, K> onlyTrashed() {
        Builder<T, K> builder = theBuilder();
        scopeSoftDeleteOnlyTrashed(builder);
        return builder;
    }

    @Override
    public Record<T, K> newRecord() {
        return new RecordBean<>(this);
    }

    @Override
    public RecordList<T, K> findAll(String... column) throws SQLRuntimeException {
        return newQuery().select(column).get();
    }

    @Override
    public RecordList<T, K> findMany(Collection<Object> ids) throws SQLRuntimeException {
        return newQuery().whereIn(getPrimaryKeyColumnName(), ids).get();
    }

    @Override
    public RecordList<T, K> findMany(Object... ids) throws SQLRuntimeException {
        return newQuery().whereIn(getPrimaryKeyColumnName(), new HashSet<>(Arrays.asList(ids))).get();
    }

    @Override
    public Record<T, K> findOrFail(Object id) throws EntityNotFoundException, SQLRuntimeException {
        return newQuery().where(getPrimaryKeyColumnName(), id).firstOrFail();
    }

    @Override
    @Nullable
    public Record<T, K> find(Object id) {
        return newQuery().where(getPrimaryKeyColumnName(), id).first();
    }

    @Override
    public Record<T, K> findOrNew(T entity) {
        // 查询是否存在满足条件的一条记录
        final Record<T, K> first = newQuery().where(entity).first();
        if (first != null) {
            return first;
        }
        // 新增此记录
        final Record<T, K> tkRecord = this.newRecord();
        tkRecord.getEntity(entity);
        return tkRecord;
    }

    @Override
    public Record<T, K> findByPrimaryKeyOrNew(T entity) {
        // 获取 entity 中的主键的值
        final Serializable primaryKeyValue = getModelShadow().getPrimaryKeyValue(entity);
        if (primaryKeyValue == null) {
            throw new PrimaryKeyNotFoundException();
        }

        // 查询是否存在满足条件的一条记录
        final Record<T, K> first = find(ObjectUtils.typeCast(primaryKeyValue));
        if (first != null) {
            return first;
        }
        // 新增此记录
        final Record<T, K> tkRecord = this.newRecord();
        tkRecord.getEntity(entity);
        return tkRecord;
    }

    @Override
    public Record<T, K> findOrCreate(T entity) {
        final Record<T, K> theRecord = findOrNew(entity);
        // 如果未持久化
        if (!theRecord.isHasBind()) {
            // 进行持久化
            theRecord.save();
        }
        return theRecord;
    }

    @Override
    public Record<T, K> findByPrimaryKeyOrCreate(T entity) {
        final Record<T, K> theRecord = findByPrimaryKeyOrNew(entity);
        // 如果未持久化
        if (!theRecord.isHasBind()) {
            // 进行持久化
            theRecord.save();
        }
        return theRecord;
    }

    @Override
    public Record<T, K> findOrNew(T conditionEntity, T complementEntity) {
        // 查询是否存在满足条件的一条记录
        final Record<T, K> first = newQuery().where(conditionEntity).first();
        if (first != null) {
            return first;
        }
        // 新增此记录
        final Record<T, K> tkRecord = this.newRecord();
        // 合并属性
        final T entityMerge = EntityUtils.entityMerge(conditionEntity, complementEntity);
        tkRecord.getEntity(entityMerge);
        return tkRecord;
    }

    @Override
    public Record<T, K> findOrCreate(T conditionEntity, T complementEntity) {
        final Record<T, K> theRecord = findOrNew(conditionEntity, complementEntity);
        // 如果未持久化
        if (!theRecord.isHasBind()) {
            // 进行持久化
            theRecord.save();
        }
        return theRecord;
    }

    @Override
    public Record<T, K> create(T entity) {
        final Record<T, K> theRecord = newRecord();
        theRecord.getEntity(entity);
        theRecord.save();
        return theRecord;
    }

    @Override
    public Record<T, K> updateByPrimaryKeyOrCreate(T entity) {
        // 获取 entity 中的主键的值
        final Serializable primaryKeyValue = getModelShadow().getPrimaryKeyValue(entity);
        if (primaryKeyValue == null) {
            throw new PrimaryKeyNotFoundException();
        }

        final Record<T, K> first = find(ObjectUtils.typeCast(primaryKeyValue));
        final Record<T, K> theRecord = first != null ? first : this.newRecord();
        theRecord.fillEntity(entity);
        theRecord.save();
        return theRecord;
    }

    @Override
    public Record<T, K> updateOrCreate(T conditionEntity, T complementEntity) {
        // 查询是否存在满足条件的一条记录
        final Record<T, K> first = newQuery().where(conditionEntity).first();
        if (first != null) {
            // 更新
            first.fillEntity(complementEntity);
            first.save();
            return first;
        } else {
            // 新增
            final Record<T, K> tkRecord = this.newRecord();
            tkRecord.getEntity(conditionEntity);
            tkRecord.fillEntity(complementEntity);
            tkRecord.save();
            return tkRecord;
        }
    }

    @Override
    public String getPrimaryKeyColumnName() throws PrimaryKeyNotFoundException {
        PrimaryKeyMember primaryKeyMember = getModelMember().getEntityMember().getPrimaryKeyMember();
        if (null == primaryKeyMember) {
            throw new PrimaryKeyNotFoundException();
        }
        return primaryKeyMember.getFieldMember().getColumnName();
    }

    @Override
    public Class<K> getPrimaryKeyClass() {
        return getModelMember().getPrimaryKeyClass();
    }

    @Override
    public String getTableName() {
        return getModelMember().getEntityMember().getTableName();
    }

    @Override
    public Class<T> getEntityClass() {
        return getModelMember().getEntityClass();
    }

    @Override
    public Container getContainer() {
        return getGaarasonDataSource().getContainer();
    }

    /**
     * 获取模型信息
     * @return 模型信息
     */
    protected ModelMember<T, K> getModelMember() {
        return getModelShadow().get(this);
    }
}
