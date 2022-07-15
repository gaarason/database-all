package gaarason.database.contract.builder;

import gaarason.database.appointment.JoinType;
import gaarason.database.contract.eloquent.Builder;
import gaarason.database.contract.function.ColumnFunctionalInterface;

/**
 * 连接
 * @param <T>
 * @param <K>
 * @author xt
 */
public interface JoinLambda<T, K> extends Join<T, K>, Support<T, K> {

    /**
     * 连接查询
     * @param table 连接的表名
     * @param column1 字段1表达式
     * @param symbol 关系
     * @param column2 字段2表达式
     * @return 查询构造器
     */
    default Builder<T, K> join(String table, ColumnFunctionalInterface<T> column1, String symbol,
        ColumnFunctionalInterface<T> column2) {
        return join(table, lambda2ColumnName(column1), symbol, lambda2ColumnName(column2));
    }

    /**
     * 连接查询
     * @param joinType 连接类型
     * @param table 连接的表名
     * @param column1 字段1表达式
     * @param symbol 关系
     * @param column2 字段2表达式
     * @return 查询构造器
     */
    default Builder<T, K> join(JoinType joinType, String table, ColumnFunctionalInterface<T> column1, String symbol,
        ColumnFunctionalInterface<T> column2) {
        return join(joinType, table, lambda2ColumnName(column1), symbol, lambda2ColumnName(column2));
    }

}
