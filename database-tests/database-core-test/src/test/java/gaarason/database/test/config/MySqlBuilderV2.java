package gaarason.database.test.config;

import gaarason.database.query.AbstractBuilder;
import gaarason.database.query.MySqlBuilder;
/**
 * 自定义查询构造器
 * @see MySqlBuilder
 */
public final class MySqlBuilderV2<T, K> extends AbstractBuilder<MySqlBuilderV2<T, K>, T, K> {

    private static final long serialVersionUID = 1L;

    /**
     * 必须实现
     */
    @Override
    public MySqlBuilderV2<T, K> getSelf() {
        return this;
    }

    /**
     * 重写原方法
     */
    @Override
    public MySqlBuilderV2<T, K> limit(Object offset, Object take) {
        return super.limit(offset, take);
    }

    /**
     * 全新的自定义发发
     */
    public MySqlBuilderV2<T, K> 自定义方法(Object something) {
        return getSelf();
    }
}
