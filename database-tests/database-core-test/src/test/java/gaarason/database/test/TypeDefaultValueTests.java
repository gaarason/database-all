package gaarason.database.test;

import gaarason.database.appointment.FinalVariable;
import gaarason.database.util.ConverterUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.time.Month;
import java.time.MonthDay;
import java.time.Year;
import java.time.YearMonth;

/**
 * 实体字段缺省值测试(纯单测, 不依赖数据库).
 * <p>
 * 缺省值在 {@code FieldMember} 构造时被无条件调用, 一旦抛异常或类型不符,
 * 含该字段的实体在模型加载期即失败.
 */
@Slf4j
@FixMethodOrder(MethodSorters.JVM)
public class TypeDefaultValueTests {

    /**
     * 基本类型与其包装类型视为同一类型
     */
    private static Class<?> wrap(Class<?> clz) {
        if (!clz.isPrimitive()) {
            return clz;
        }
        if (boolean.class.equals(clz)) {
            return Boolean.class;
        }
        if (byte.class.equals(clz)) {
            return Byte.class;
        }
        if (char.class.equals(clz)) {
            return Character.class;
        }
        if (short.class.equals(clz)) {
            return Short.class;
        }
        if (int.class.equals(clz)) {
            return Integer.class;
        }
        if (long.class.equals(clz)) {
            return Long.class;
        }
        if (float.class.equals(clz)) {
            return Float.class;
        }
        if (double.class.equals(clz)) {
            return Double.class;
        }
        return clz;
    }

    /**
     * 所有被允许的字段类型, 缺省值必须能安全产出, 且类型与请求类型一致
     */
    @Test
    public void 允许类型的缺省值类型一致() {
        for (Class<?> clz : FinalVariable.ALLOW_FIELD_TYPES) {
            Object value;
            try {
                value = ConverterUtils.getDefaultValueByJavaType(clz);
            } catch (Throwable e) {
                Assert.fail("获取缺省值失败: " + clz.getName() + " -> " + e);
                return;
            }
            if (value == null) {
                continue;
            }
            Assert.assertTrue(wrap(clz).getName() + " 的缺省值应为该类型, 实际为 " + value.getClass().getName(),
                wrap(clz).isAssignableFrom(value.getClass()));
        }
    }

    /**
     * short 的缺省值必须是 Short
     */
    @Test
    public void short缺省值为Short() {
        Short value = ConverterUtils.getDefaultValueByJavaType(Short.class);
        Assert.assertEquals(Short.valueOf((short) 0), value);

        short primitive = ConverterUtils.getDefaultValueByJavaType(short.class);
        Assert.assertEquals((short) 0, primitive);
    }

    /**
     * 日期派生类型的缺省值不能从 LocalTime 推导(会抛 DateTimeException)
     */
    @Test
    public void 年月日缺省值可用() {
        Assert.assertTrue(ConverterUtils.getDefaultValueByJavaType(Year.class) instanceof Year);
        Assert.assertTrue(ConverterUtils.getDefaultValueByJavaType(YearMonth.class) instanceof YearMonth);
        Assert.assertTrue(ConverterUtils.getDefaultValueByJavaType(Month.class) instanceof Month);
        Assert.assertTrue(ConverterUtils.getDefaultValueByJavaType(MonthDay.class) instanceof MonthDay);
    }
}
