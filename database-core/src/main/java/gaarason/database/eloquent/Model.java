package gaarason.database.eloquent;

import gaarason.database.connections.ProxyDataSource;
import gaarason.database.core.lang.Nullable;
import gaarason.database.eloquent.repository.BaseSoftDeleting;
import gaarason.database.exception.EntityNotFoundException;
import gaarason.database.exception.SQLRuntimeException;
import gaarason.database.query.Builder;
import gaarason.database.query.MySqlBuilder;

abstract public class Model<T, K> extends BaseSoftDeleting<T, K> {

    /**
     * @return dataSource代理
     */
    abstract public ProxyDataSource getProxyDataSource();

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
        ProxyDataSource proxyDataSource = getProxyDataSource();
        return apply(new MySqlBuilder<>(proxyDataSource, this, entityClass));
    }

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

}
