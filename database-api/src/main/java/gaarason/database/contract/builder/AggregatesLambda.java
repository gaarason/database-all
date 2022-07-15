package gaarason.database.contract.builder;

import gaarason.database.appointment.AggregatesType;
import gaarason.database.contract.function.ColumnFunctionalInterface;

import java.math.BigDecimal;

/**
 * 统计
 * @author xt
 */
public interface AggregatesLambda<T, K> extends Aggregates, Support<T, K> {

    /**
     * 统计
     * @param op 操作类型
     * @param column 统计字段表达式
     * @param <R> 响应类型
     * @return 响应
     */
    default <R> R aggregate(AggregatesType op, ColumnFunctionalInterface<T> column) {
        return aggregate(op, lambda2ColumnName(column));
    }

    /**
     * count 条数统计,兼容 group
     * @param column 统计字段表达式
     * @return 计数
     */
    default Long count(ColumnFunctionalInterface<T> column) {
        return count(lambda2FieldName(column));
    }

    /**
     * 求最大值
     * @param column 统计字段表达式
     * @return 最大值
     */
    default String max(ColumnFunctionalInterface<T> column) {
        return max(lambda2ColumnName(column));
    }

    /**
     * 求最小值
     * @param column 统计字段表达式
     * @return 最小值
     */
    default String min(ColumnFunctionalInterface<T> column) {
        return min(lambda2ColumnName(column));
    }

    /**
     * 求平均值
     * @param column 统计字段表达式
     * @return 平均值
     */
    default BigDecimal avg(ColumnFunctionalInterface<T> column) {
        return avg(lambda2ColumnName(column));
    }

    /**
     * 求和
     * @param column 统计字段表达式
     * @return 总和
     */
    default BigDecimal sum(ColumnFunctionalInterface<T> column) {
        return sum(lambda2ColumnName(column));
    }
}
