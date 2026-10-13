package gaarason.database.test;

import gaarason.database.appointment.AggregatesType;
import gaarason.database.contract.builder.Aggregates;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * 聚合结果类型规范化测试
 * <p>
 * 不同数据库驱动对聚合函数的返回类型不同(如 MySQL 的 COUNT() 返回 Long, SQLite 的 COUNT() 返回 Integer),
 * 契约层应将结果统一规范为接口声明的类型, 避免 ClassCastException(如分页时 Long total = builder.count())
 */
public class AggregatesTests {

    /**
     * 模拟不同数据库驱动返回类型的统计实现
     */
    private static class MockAggregates implements Aggregates {

        final Object value;

        MockAggregates(Object value) {
            this.value = value;
        }

        @SuppressWarnings("unchecked")
        @Override
        public <R> R aggregate(AggregatesType op, String column) {
            return (R) value;
        }
    }

    @Test
    public void countTypeCompatibility() {
        // MySQL 驱动: COUNT() 返回 Long
        Assert.assertEquals(Long.valueOf(10), new MockAggregates(10L).count());
        // SQLite 驱动: COUNT() 返回 Integer
        Assert.assertEquals(Long.valueOf(10), new MockAggregates(10).count());
        // 其他数值类型
        Assert.assertEquals(Long.valueOf(10), new MockAggregates(BigInteger.TEN).count());
        // 字符串类型的数字
        Assert.assertEquals(Long.valueOf(10), new MockAggregates("10").count());
    }

    @Test
    public void avgTypeCompatibility() {
        // MySQL 驱动: AVG() 返回 BigDecimal
        Assert.assertEquals(new BigDecimal("2.5"), new MockAggregates(new BigDecimal("2.5")).avg("age"));
        // SQLite 驱动: AVG() 返回 Double
        Assert.assertEquals(new BigDecimal("2.5"), new MockAggregates(2.5d).avg("age"));
        // 整型返回
        Assert.assertEquals(new BigDecimal("2"), new MockAggregates(2).avg("age"));
        // 空结果
        Assert.assertNull(new MockAggregates(null).avg("age"));
    }

    @Test
    public void sumTypeCompatibility() {
        // MySQL 驱动: SUM() 返回 BigDecimal
        Assert.assertEquals(new BigDecimal("10"), new MockAggregates(new BigDecimal("10")).sum("age"));
        // SQLite 驱动: SUM() 对整数列返回 Long/Integer
        Assert.assertEquals(new BigDecimal("10"), new MockAggregates(10L).sum("age"));
        Assert.assertEquals(new BigDecimal("10"), new MockAggregates(10).sum("age"));
        // 浮点数无精度损失
        Assert.assertEquals(new BigDecimal("0.1"), new MockAggregates(0.1d).sum("age"));
        // 空结果
        Assert.assertNull(new MockAggregates(null).sum("age"));
    }
}
