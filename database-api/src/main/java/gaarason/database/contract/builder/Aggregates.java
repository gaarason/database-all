package gaarason.database.contract.builder;

import gaarason.database.appointment.AggregatesType;

import java.math.BigDecimal;

/**
 * 统计
 * @author xt
 */
public interface Aggregates {

    /**
     * 统计
     * @param op 操作类型
     * @param column 统计字段
     * @param <R> 响应类型
     * @return 响应
     */
    <R> R aggregate(AggregatesType op, String column);

    /**
     * count(*) 条数统计,兼容 group
     * @return 计数
     */
    default Long count() {
        return count("*");
    }

    /**
     * count 条数统计,兼容 group
     * @param column 统计字段
     * @return 计数
     */
    default Long count(String column) {
        return aggregate(AggregatesType.count, column);
    }

    /**
     * 求最大值
     * @param column 统计字段
     * @return 最大值
     */
    default String max(String column) {
        Object aggregate = aggregate(AggregatesType.max, column);
        return String.valueOf(aggregate);
    }

    /**
     * 求最小值
     * @param column 统计字段
     * @return 最小值
     */
    default String min(String column) {
        Object aggregate = aggregate(AggregatesType.min, column);
        return String.valueOf(aggregate);
    }

    /**
     * 求平均值
     * @param column 统计字段
     * @return 平均值
     */
    default BigDecimal avg(String column) {
        return aggregate(AggregatesType.avg, column);
    }

    /**
     * 求和
     * @param column 统计字段
     * @return 总和
     */
    default BigDecimal sum(String column) {
        return aggregate(AggregatesType.sum, column);
    }
}
