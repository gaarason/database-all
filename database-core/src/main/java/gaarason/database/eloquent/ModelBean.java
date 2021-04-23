package gaarason.database.eloquent;

import gaarason.database.contract.connection.GaarasonDataSource;
import gaarason.database.contract.eloquent.Builder;
import gaarason.database.contract.eloquent.Model;
import gaarason.database.contract.eloquent.Record;
import gaarason.database.contract.eloquent.RecordList;
import gaarason.database.contract.support.IdGenerator;
import gaarason.database.core.lang.Nullable;
import gaarason.database.exception.EntityNotFoundException;
import gaarason.database.exception.PrimaryKeyNotFoundException;
import gaarason.database.exception.SQLRuntimeException;
import gaarason.database.provider.ModelShadowProvider;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

abstract public class ModelBean<T, K> implements Model<T, K> {

    public ModelBean() {

    }

    /**
     * @return dataSource代理
     */
    abstract protected GaarasonDataSource getGaarasonDataSource();

    @Override
    public int delete(Builder<T, K> builder) {
        return softDeleting() ? softDelete(builder) : builder.forceDelete();
    }

    @Override
    public int restore(Builder<T, K> builder) {
        return softDeleteRestore(builder);
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
        builder.where("is_deleted", "1");
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
        builder.where("is_deleted", "0");
    }


    /**
     * 软删除实现
     * @param builder 查询构造器
     * @return 删除的行数
     */
    protected int softDelete(Builder<T, K> builder) {
        return builder.data("is_deleted", "1").update();
    }

    /**
     * 恢复软删除实现
     * @param builder 查询构造器
     * @return 恢复的行数
     */
    protected int softDeleteRestore(Builder<T, K> builder) {
        return builder.data("is_deleted", "0").update();
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
        return apply(gaarasonDataSource.getDatabaseType().getBuilderByDatabaseType(gaarasonDataSource, this, getEntityClass()));
//        return apply(new MySqlBuilder<>(gaarasonDataSource, this, getEntityClass()));
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
        return new RecordBean<>(getEntityClass(), this);
    }

    @Override
    public RecordList<T, K> findAll(String... column) throws SQLRuntimeException {
        return newQuery().select(column).get();
    }

    @Override
    public RecordList<T, K> findMany(Collection<K> ids) throws SQLRuntimeException {
        return newQuery().whereIn(getPrimaryKeyColumnName(), ids).get();
    }

    @SafeVarargs
    @Override
    public final RecordList<T, K> findMany(K... ids) throws SQLRuntimeException {
        return newQuery().whereIn(getPrimaryKeyColumnName(), new HashSet<>(Arrays.asList(ids))).get();
    }

    @Override
    public Record<T, K> findOrFail(K id) throws EntityNotFoundException, SQLRuntimeException {
        return newQuery().where(getPrimaryKeyColumnName(), String.valueOf(id)).firstOrFail();
    }

    @Override
    @Nullable
    public Record<T, K> find(K id) {
        return newQuery().where(getPrimaryKeyColumnName(), String.valueOf(id)).first();
    }

    @Override
    public boolean isPrimaryKeyDefinition() {
        return ModelShadowProvider.get(this).isPrimaryKeyDefinition();
    }

    @Override
    public String getPrimaryKeyColumnName() throws PrimaryKeyNotFoundException {
        String primaryKeyColumnName = ModelShadowProvider.get(this).getPrimaryKeyColumnName();
        if (null == primaryKeyColumnName) {
            throw new PrimaryKeyNotFoundException();
        }
        return primaryKeyColumnName;
    }

    @Override
    public String getPrimaryKeyName() throws PrimaryKeyNotFoundException {
        String primaryKeyName = ModelShadowProvider.get(this).getPrimaryKeyName();
        if (null == primaryKeyName) {
            throw new PrimaryKeyNotFoundException();
        }
        return primaryKeyName;
    }

    @Override
    public boolean isPrimaryKeyIncrement() throws PrimaryKeyNotFoundException {
        Boolean primaryKeyIncrement = ModelShadowProvider.get(this).getPrimaryKeyIncrement();
        if (null == primaryKeyIncrement) {
            throw new PrimaryKeyNotFoundException();
        }
        return primaryKeyIncrement;
    }

    @Override
    public ModelShadowProvider.FieldInfo getPrimaryKeyFieldInfo() throws PrimaryKeyNotFoundException {
        ModelShadowProvider.FieldInfo primaryKeyFieldInfo = ModelShadowProvider.get(this).getPrimaryKeyFieldInfo();
        if (null == primaryKeyFieldInfo) {
            throw new PrimaryKeyNotFoundException();
        }
        return primaryKeyFieldInfo;
    }

    @Override
    public IdGenerator<K> getPrimaryKeyIdGenerator() throws PrimaryKeyNotFoundException {
        IdGenerator<K> primaryKeyIdGenerator = ModelShadowProvider.get(this).getPrimaryKeyIdGenerator();
        if (null == primaryKeyIdGenerator) {
            throw new PrimaryKeyNotFoundException();
        }
        return primaryKeyIdGenerator;
    }

    @Override
    public Class<K> getPrimaryKeyClass() {
        return ModelShadowProvider.get(this).getPrimaryKeyClass();
    }

    @Override
    public String getTableName() {
        return ModelShadowProvider.get(this).getTableName();
    }

    @Override
    public Class<T> getEntityClass() {
        return ModelShadowProvider.get(this).getEntityClass();
    }
}
