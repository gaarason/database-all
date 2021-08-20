package gaarason.database.contract.builder;

import gaarason.database.eloquent.appointment.AggregatesType;

import java.math.BigDecimal;

/**
 * 统计
 * @author xt
 */
public interface Aggregates {

    /**
     * 统计
     * @param op     操作类型
     * @param column 统计字段
     * @param <R>    响应类型
     * @return 响应
     */
    <R> R aggregate(AggregatesType op, String column);

    /**
     * count(*) 条数统计,兼容 group
     * @return 计数
     */
    Long count();

    /**
     * count 条数统计,兼容 group
     * @param column 统计字段
     * @return 计数
     */
    Long count(String column);

    /**
     * 求最大值
     * @param column 统计字段
     * @return 最大值
     */
    String max(String column);

    /**
     * 求最小值
     * @param column 统计字段
     * @return 最小值
     */
    String min(String column);

    /**
     * 求平均值
     * @param column 统计字段
     * @return 平均值
     */
    BigDecimal avg(String column);

    /**
     * 求和
     * @param column 统计字段
     * @return 总和
     */
    BigDecimal sum(String column);
}
