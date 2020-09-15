package gaarason.database.eloquent;

import gaarason.database.contract.GaarasonDataSource;
import gaarason.database.contract.eloquent.Builder;
import gaarason.database.contract.eloquent.Model;
import gaarason.database.core.lang.Nullable;
import gaarason.database.eloquent.annotations.Primary;
import gaarason.database.exception.EntityNotFoundException;
import gaarason.database.exception.InvalidPrimaryKeyTypeException;
import gaarason.database.exception.MethodNotSupportedException;
import gaarason.database.exception.SQLRuntimeException;
import gaarason.database.query.MySqlBuilder;
import gaarason.database.util.EntityUtil;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;

abstract public class GaarasonModel<T, K> implements Model<T, K> {

    /**
     * 主键列名(并非一定是实体的属性名)
     */
    protected String primaryKeyColumnName;

    /**
     * 主键名(实体的属性名)
     */
    protected String primaryKeyName;

    /**
     * 主键自增
     */
    protected boolean primaryKeyIncrement;

    /**
     * 主键类型
     */
    protected Class<K> primaryKeyClass;

    /**
     * 数据库表名
     */
    protected String tableName;

    /**
     * 实体类型
     */
    protected Class<T> entityClass;

    GaarasonModel() {
        // 泛型初始化
        genericInitialization();
        // entity分析
        analysisEntityClass();
    }

    /**
     * 泛型初始化
     */
    @SuppressWarnings("unchecked")
    protected void genericInitialization() {
        entityClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        primaryKeyClass =
            (Class<K>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[1];
    }

    /**
     * 实体类型分析
     */
    protected void analysisEntityClass() {
        tableName = EntityUtil.tableName(entityClass);
        Field[] fields = entityClass.getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Primary.class)) {
                Primary primary = field.getAnnotation(Primary.class);
                primaryKeyIncrement = primary.increment();
                primaryKeyColumnName = EntityUtil.columnName(field);
                primaryKeyName = field.getName();
                // 主键类型检测
                if (!primaryKeyClass.equals(field.getType())) {
                    throw new InvalidPrimaryKeyTypeException(
                        "The primary key type [" + field.getType() + "] of the entity does not match with the " +
                            "generic [" + primaryKeyClass + "]");
                }
                break;
            }
        }
    }


    protected ThreadLocal<Record<T, K>> newRecordForShortcut = new ThreadLocal<>();

    @Override
    public T getEntity() {
        Record<T, K> newRecord = newRecord();
        newRecordForShortcut.set(newRecord);
        return newRecord.getEntity();
    }

    @Override
    public boolean save() {
        try {
            return newRecordForShortcut.get().save();
        } finally {
            newRecordForShortcut.remove();
        }
    }

    @Override
    public boolean delete() {
        throw new MethodNotSupportedException();
    }

    @Override
    public boolean restore() {
        throw new MethodNotSupportedException();
    }

    @Override
    public boolean restore(boolean refresh) {
        throw new MethodNotSupportedException();
    }

    @Override
    public Record<T, K> refresh() {
        throw new MethodNotSupportedException();
    }


    /**
     * 是否启用软删除
     */
    protected boolean softDeleting() {
        return false;
    }

    /**
     * 删除(软/硬删除)
     * @param builder 查询构造器
     * @return 删除的行数
     */
    public int delete(Builder<T, K> builder) {
        return softDeleting() ? softDelete(builder) : builder.forceDelete();
    }

    /**
     * 恢复软删除
     * @param builder 查询构造器
     * @return 删除的行数
     */
    public int restore(Builder<T, K> builder) {
        return softDeleteRestore(builder);
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
        // todo 按连接类型,等等信息选择 builder
        GaarasonDataSource gaarasonDataSource = getGaarasonDataSource();
        return apply(new MySqlBuilder<>(gaarasonDataSource, this, entityClass));
    }


    /**
     * @return dataSource代理
     */
    abstract public GaarasonDataSource getGaarasonDataSource();

    /**
     * 新的查询构造器
     * @return 查询构造器
     */
    public Builder<T, K> newQuery() {
        Builder<T, K> builder = theBuilder();
        if (softDeleting()) {
            scopeSoftDelete(builder);
        }
        return builder;
    }

    /**
     * 包含软删除模型
     * @return 查询构造器
     */
    public Builder<T, K> withTrashed() {
        Builder<T, K> builder = theBuilder();
        scopeSoftDeleteWithTrashed(builder);
        return builder;
    }

    /**
     * 只获取软删除模型
     * @return 查询构造器
     */
    public Builder<T, K> onlyTrashed() {
        Builder<T, K> builder = theBuilder();
        scopeSoftDeleteOnlyTrashed(builder);
        return builder;
    }

    /**
     * 新的记录对象
     * @return 记录对象
     */
    public Record<T, K> newRecord() {
        return new Record<>(entityClass, this);
    }


    public RecordList<T, K> all(String... column) throws SQLRuntimeException {
        return newQuery().select(column).get();
    }

    public Record<T, K> findOrFail(K id) throws EntityNotFoundException, SQLRuntimeException {
        return newQuery().where(primaryKeyColumnName, id.toString()).firstOrFail();
    }

    @Nullable
    public Record<T, K> find(K id) {
        return newQuery().where(primaryKeyColumnName, id.toString()).first();
    }

    @Override
    public String getPrimaryKeyColumnName() {
        return primaryKeyColumnName;
    }

    @Override
    public String getPrimaryKeyName() {
        return primaryKeyName;
    }

    @Override
    public boolean isPrimaryKeyIncrement() {
        return primaryKeyIncrement;
    }

    @Override
    public Class<K> getPrimaryKeyClass() {
        return primaryKeyClass;
    }

    @Override
    public String getTableName() {
        return tableName;
    }

    @Override
    public Class<T> getEntityClass() {
        return entityClass;
    }
}
