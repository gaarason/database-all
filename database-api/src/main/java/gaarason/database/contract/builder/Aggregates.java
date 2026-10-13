package gaarason.database.contract.builder;

import gaarason.database.appointment.AggregatesType;

import java.math.BigDecimal;
import java.math.BigInteger;

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
        Object aggregate = aggregate(AggregatesType.count, column);
        if (null == aggregate) {
            return null;
        }
        // 不同数据库驱动对 COUNT() 的返回类型不同(如 MySQL 为 Long, SQLite 为 Integer), 统一转换为 Long
        if (aggregate instanceof Number) {
            return ((Number) aggregate).longValue();
        }
        return Long.valueOf(aggregate.toString());
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
        return toBigDecimal(aggregate(AggregatesType.avg, column));
    }

    /**
     * 求和
     * @param column 统计字段
     * @return 总和
     */
    default BigDecimal sum(String column) {
        return toBigDecimal(aggregate(AggregatesType.sum, column));
    }

    /**
     * 将聚合结果转换为 BigDecimal
     * <p>
     * 不同数据库驱动对 AVG()/SUM() 的返回类型不同(如 Double/Long/BigInteger/BigDecimal), 统一转换为 BigDecimal
     * @param value 聚合结果
     * @return BigDecimal, 空结果时为 null
     */
    static BigDecimal toBigDecimal(Object value) {
        if (null == value) {
            return null;
        }
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }
        if (value instanceof BigInteger) {
            return new BigDecimal((BigInteger) value);
        }
        // 浮点数经由字符串转换, 避免二进制浮点精度损失
        return new BigDecimal(value.toString());
    }
}
