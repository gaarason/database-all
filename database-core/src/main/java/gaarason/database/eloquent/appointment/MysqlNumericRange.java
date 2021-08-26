package gaarason.database.eloquent.appointment;

import lombok.Getter;

/**
 * Mysql 数字类型取值范围
 * @author xt
 */
@Getter
public enum MysqlNumericRange {

    /**
     * 范围
     */
    TINYINT("tinyint", Byte.MAX_VALUE, Byte.MIN_VALUE, Integer.class),
    TINYINT_UNSIGNED("tinyint unsigned", Byte.MAX_VALUE - Byte.MIN_VALUE, 0, Integer.class),
    SMALLINT("smallint", 32767, -32768, Integer.class),
    SMALLINT_UNSIGNED("smallint unsigned", 65535, 0, Integer.class),
    MEDIUMINT("mediumint", 8388607, -8388608, Integer.class),
    MEDIUMINT_UNSIGNED("mediumint unsigned", 16777215, 0, Integer.class),
    INT("int", Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.class),
    INT_UNSIGNED("int unsigned",
        Long.parseLong(String.valueOf(Integer.MAX_VALUE)) - Long.parseLong(String.valueOf(Integer.MIN_VALUE)), 0,
        Long.class),
    BIGINT("bigint", Long.MAX_VALUE, Long.MIN_VALUE, Long.class),
    // 缩减了 bigint unsigned 的值为原值的一半
    BIGINT_UNSIGNED("bigint unsigned", Long.MAX_VALUE, 0, Long.class);

    private final String mysqlColumnTypeName;

    private final long max;

    private final long min;

    private final Class<?> javaClassType;

    /**
     * @param mysqlColumnTypeName 字符的数据中的类型
     * @param max                 最大值
     * @param min                 最小值
     * @param javaClassType       java类型
     */
    MysqlNumericRange(String mysqlColumnTypeName, long max, long min, Class<?> javaClassType) {
        this.mysqlColumnTypeName = mysqlColumnTypeName;
        this.max = max;
        this.min = min;
        this.javaClassType = javaClassType;
    }

}
