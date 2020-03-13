package gaarason.database.contracts.builder;

import gaarason.database.eloquent.enums.JoinType;
import gaarason.database.query.Builder;

/**
 * 连接
 * @param <T>
 */
public interface Join<T> {

    /**
     * 连接查询
     * @param table 连接的表名
     * @param column1 字段1
     * @param symbol 关系
     * @param column2 字段2
     * @return 查询构造器
     */
    Builder<T> join(String table, String column1, String symbol, String column2);

    /**
     * 连接查询
     * @param joinType 连接类型
     * @param table 连接的表名
     * @param column1 字段1
     * @param symbol 关系
     * @param column2 字段2
     * @return 查询构造器
     */
    Builder<T> join(JoinType joinType, String table, String column1, String symbol,
                    String column2);

}
