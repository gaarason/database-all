package gaarason.database.contract.builder;

import gaarason.database.appointment.AggregatesType;
import gaarason.database.contract.eloquent.Builder;
import gaarason.database.contract.function.ColumnFunctionalInterface;

import java.math.BigDecimal;

/**
 * 统计
 * @author xt
 */
public interface AggregatesLambda<B extends Builder<B, T, K>, T, K> extends Aggregates, Support<B, T, K> {

    /**
     * 统计
     * @param op 操作类型
     * @param column 统计字段表达式
     * @param <F> 属性类型
     * @param <R> 响应类型
     * @return 响应
     */
    default <F, R> R aggregate(AggregatesType op, ColumnFunctionalInterface<T, F> column) {
        return aggregate(op, lambda2ColumnName(column));
    }

    /**
     * count 条数统计,兼容 group
     * @param column 统计字段表达式
     * @param <F> 属性类型
     * @return 计数
     */
    default <F> Long count(ColumnFunctionalInterface<T, F> column) {
        return count(lambda2FieldName(column));
    }

    /**
     * 求最大值
     * @param column 统计字段表达式
     * @param <F> 属性类型
     * @return 最大值
     */
    default <F> String max(ColumnFunctionalInterface<T, F> column) {
        return max(lambda2ColumnName(column));
    }

    /**
     * 求最小值
     * @param column 统计字段表达式
     * @param <F> 属性类型
     * @return 最小值
     */
    default <F> String min(ColumnFunctionalInterface<T, F> column) {
        return min(lambda2ColumnName(column));
    }

    /**
     * 求平均值
     * @param column 统计字段表达式
     * @param <F> 属性类型
     * @return 平均值
     */
    default <F> BigDecimal avg(ColumnFunctionalInterface<T, F> column) {
        return avg(lambda2ColumnName(column));
    }

    /**
     * 求和
     * @param column 统计字段表达式
     * @param <F> 属性类型
     * @return 总和
     */
    default <F> BigDecimal sum(ColumnFunctionalInterface<T, F> column) {
        return sum(lambda2ColumnName(column));
    }
}
