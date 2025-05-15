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
     * 是否插入/更新/条件使用, 当对应策略值为DEFAULT(未配置)时, 取用本策略
     */
    Class<? extends FieldStrategy> strategy() default FieldStrategy.NotNull.class;

    /**
     * 是否插入时使用
     * 当值为默认时, 取用 strategy() 的值
     */
    Class<? extends FieldStrategy> insertStrategy() default FieldStrategy.Default.class;

    /**
     * 是否更新时使用
     * 当值为默认时, 取用 strategy() 的值
     */
    Class<? extends FieldStrategy> updateStrategy() default FieldStrategy.Default.class;

    /**
     * 是否条件时使用
     * 当值为默认时, 取用 strategy() 的值
     */
    Class<? extends FieldStrategy> conditionStrategy() default FieldStrategy.Default.class;

    /**
     * 字段填充策略
     */
    Class<? extends FieldFill> fill() default FieldFill.NotFill.class;

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
     * 自动模式 (默认)
     * 1.如果是枚举类型, 那么以 `枚举类型的自然次序` 进行序列化与反序列化
     * 2.如果是集合类型, 且集合内为数字, 那么以 `位` 进行序列化与反序列化
     * 3.如果是基本类型, 那么进行普通序列化与反序列化
     * 4.兜底使用Json序列化 (实现依赖于jackson, 需要手动引入 com.fasterxml.jackson.core: jackson-databind 与 com.fasterxml.jackson.datatype: jackson-datatype-jsr310 依赖)
     * 手动指定
     * 1.自定义实现 FieldConversion 接口即可
     */
    Class<? extends FieldConversion> conversion() default FieldConversion.Auto.class;
}
