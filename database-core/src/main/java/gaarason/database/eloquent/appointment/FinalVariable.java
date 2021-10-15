package gaarason.database.eloquent.appointment;

import gaarason.database.eloquent.annotation.BelongsTo;
import gaarason.database.eloquent.annotation.BelongsToMany;
import gaarason.database.eloquent.annotation.HasOneOrMany;

import java.lang.annotation.Annotation;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * 全局可用的常量
 * @author xt
 */
public class FinalVariable {

    /**
     * 实体中普通属性支持的包装类型
     */
    public static final List<Class<?>> ALLOW_FIELD_TYPES = Arrays.asList(Boolean.class, boolean.class, Byte.class, byte.class, Character.class,
        char.class, Short.class, short.class, Integer.class, int.class, Long.class, long.class, Float.class, float.class, Double.class, double.class,
        BigInteger.class, Number.class, java.sql.Date.class, Time.class, Timestamp.class, Date.class, LocalDate.class, LocalTime.class,
        LocalDateTime.class, String.class, BigDecimal.class, Blob.class, Clob.class);

    /**
     * 关联关系声明注解
     */
    public static final List<Class<? extends Annotation>> RELATION_ANNOTATIONS = Arrays.asList(HasOneOrMany.class,
        BelongsTo.class, BelongsToMany.class);

    /**
     * 在使用闭包事务时, 发生死锁异常后的默认重试次数
     */
    public static final int DEFAULT_CAUSED_BY_DEADLOCK_RETRY_COUNT = 2;

    /**
     * 链接中的元数据字段
     */
    public static final List<String> metaDataLabel = Arrays.asList("TABLE_CAT", "TABLE_SCHEM", "TABLE_NAME", "COLUMN_NAME",
        "DATA_TYPE", "COLUMN_SIZE", "BUFFER_LENGTH", "DECIMAL_DIGITS", "NUM_PREC_RADIX", "NULLABLE", "REMARKS", "COLUMN_DEF",
        "SQL_DATA_TYPE", "SQL_DATETIME_SUB", "CHAR_OCTET_LENGTH", "ORDINAL_POSITION", "IS_NULLABLE", "SCOPE_CATALOG",
        "SCOPE_SCHEMA", "SCOPE_TABLE", "SOURCE_DATA_TYPE", "IS_AUTOINCREMENT", "IS_GENERATEDCOLUMN");

    private FinalVariable() {

    }
}
