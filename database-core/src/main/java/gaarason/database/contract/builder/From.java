package gaarason.database.contract.builder;

import gaarason.database.contract.eloquent.Builder;
import gaarason.database.contract.function.GenerateSqlPartFunctionalInterface;

/**
 * 数据表
 * @param <T>
 * @param <K>
 */
public interface From<T, K> {
    /**
     * 更改查询的表名
     * @param table 表名
     * @return 查询构建器
     */
    Builder<T, K> from(String table);

    /**
     * 临时表查询
     * @param alias 临时表别名
     * @param closure 闭包
     * @return 查询构建器
     */
    Builder<T, K> from(String alias, GenerateSqlPartFunctionalInterface closure);

    /**
     * 临时表查询
     * @param alias 临时表别名
     * @param sql 完整查询语句
     * @return 查询构建器
     */
    Builder<T, K> from(String alias, String sql);
}
