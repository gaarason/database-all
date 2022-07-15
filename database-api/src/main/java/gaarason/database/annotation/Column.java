package gaarason.database.annotation;

import gaarason.database.appointment.FieldStrategy;
import gaarason.database.contract.support.FieldFill;

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
     * 是否插入时使用
     */
    FieldStrategy insertStrategy() default FieldStrategy.DEFAULT;

    /**
     * 是否更新时使用
     */
    FieldStrategy updateStrategy() default FieldStrategy.DEFAULT;

    /**
     * 是否条件时使用
     */
    FieldStrategy conditionStrategy() default FieldStrategy.DEFAULT;

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
     * java对象序列化到数据库
     */
    Class<?> serializer() default Object.class;

    /**
     * 从数据库反序列化到java对象
     */
    Class<?> deserializer() default Object.class;
}
