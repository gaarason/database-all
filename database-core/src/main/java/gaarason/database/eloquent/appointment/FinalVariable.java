package gaarason.database.eloquent.appointment;

import gaarason.database.eloquent.annotation.BelongsTo;
import gaarason.database.eloquent.annotation.BelongsToMany;
import gaarason.database.eloquent.annotation.HasOneOrMany;

import java.lang.annotation.Annotation;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * 全局可用的常量
 */
public class FinalVariable {

    /**
     * 实体中普通属性支持的包装类型
     */
    public final static List<Class<?>> allowFieldTypes = Arrays.asList(Boolean.class, Byte.class, Character.class, Short.class,
        Integer.class, Long.class, Float.class, Double.class, BigInteger.class, Date.class, String.class);

    /**
     * 关联关系声明注解
     */
    public final static List<Class<? extends Annotation>> relationAnnotations = Arrays.asList(HasOneOrMany.class,
        BelongsTo.class, BelongsToMany.class);

    /**
     * 在使用闭包事务时, 发生死锁异常后的默认重试次数
     */
    public final static int defaultCausedByDeadlockRetryCount = 2;

    /**
     * 链接中的元数据字段
     */
    public final static List<String> metaDataLabel = Arrays.asList("TABLE_CAT", "TABLE_SCHEM", "TABLE_NAME", "COLUMN_NAME",
        "DATA_TYPE", "COLUMN_SIZE", "BUFFER_LENGTH", "DECIMAL_DIGITS", "NUM_PREC_RADIX", "NULLABLE", "REMARKS", "COLUMN_DEF",
        "SQL_DATA_TYPE", "SQL_DATETIME_SUB", "CHAR_OCTET_LENGTH", "ORDINAL_POSITION", "IS_NULLABLE", "SCOPE_CATALOG",
        "SCOPE_SCHEMA", "SCOPE_TABLE", "SOURCE_DATA_TYPE", "IS_AUTOINCREMENT", "IS_GENERATEDCOLUMN");
}
