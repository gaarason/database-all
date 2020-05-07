package gaarason.database.contracts.builder;

import gaarason.database.query.Builder;

/**
 * 数据表
 * @param <T, K>
 */
public interface From<T, K> {
    /**
     * 更改查询的表名
     * @param table 表名
     * @return 查询构建器
     */
    Builder<T, K> from(String table);
}
