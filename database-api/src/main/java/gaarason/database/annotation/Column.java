package gaarason.database.annotation;

import gaarason.database.contract.support.FieldConversion;
import gaarason.database.contract.support.FieldFill;
import gaarason.database.contract.support.FieldStrategy;

import java.lang.annotation.*;
import java.sql.JDBCType;

/**
 * 数据库列(字段)注解
 * @author xt
 */
@Documented
@Inherited
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Column {

    /**
     * 数据库列名
     * 默认使用字段名转化为下线风格, eg: student_id
     */
    String name() default "";

    /**
     * 是否为数据库中的列
     */
    boolean inDatabase() default true;

    /**
     * 数据库中的默认值
     */
    String columnDefinition() default "";

    /**
     * 是否无符号
     */
    boolean unsigned() default false;

    /**
     * 数据库中, 此字段可否为 null
     */
    boolean nullable() default false;

    /**
     * 是否查询该字段
     */
    boolean selectable() default true;

    /**
     * 是否插入/更新/条件使用, 当对应策略值为DEFAULT时, 取用
     */
    Class<? extends FieldStrategy> strategy() default FieldStrategy.NotNull.class;

    /**
     * 是否插入时使用
     */
    Class<? extends FieldStrategy> insertStrategy() default FieldStrategy.Default.class;

    /**
     * 是否更新时使用
     */
    Class<? extends FieldStrategy> updateStrategy() default FieldStrategy.Default.class;

    /**
     * 是否条件时使用
     */
    Class<? extends FieldStrategy> conditionStrategy() default FieldStrategy.Default.class;

    /**
     * 字段填充策略
     */
    Class<? extends FieldFill> fill() default FieldFill.DefaultFieldFill.class;

    /**
     * (Optional) The column length. (Applies only if a
     * string-valued column is used.)
     */
    long length() default 255;

    /**
     * (Optional) The precision for a decimal (exact numeric)
     * column. (Applies only if a decimal column is used.)
     * Value must be set by developer if used when generating
     * the DDL for the column.
     */
    int precision() default 0;

    /**
     * (Optional) The scale for a decimal (exact numeric) column.
     * (Applies only if a decimal column is used.)
     */
    int scale() default 0;

    /**
     * 注释
     */
    String comment() default "";

    /**
     * 数据库中的数据类型
     */
    JDBCType jdbcType() default JDBCType.JAVA_OBJECT;

    /**
     * 序列与反序列化
     */
    Class<? extends FieldConversion> conversion() default FieldConversion.Default.class;
}
