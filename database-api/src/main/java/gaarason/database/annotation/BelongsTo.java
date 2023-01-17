package gaarason.database.annotation;

import java.lang.annotation.*;

/**
 * 类属性必须是 单数
 * @author xt
 */
@Documented
@Inherited
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface BelongsTo {

    /**
     * `本表`中的`关联父表的外键`
     * @return `本表`中的`关联父表的外键`
     */
    String localModelForeignKey();

    /**
     * `父表`中的`关联键`
     * 默认值为`父表`的主键(`@Primary()`修饰的键)
     * @return `父表`中的`关联键`
     */
    String parentModelLocalKey() default "";

    /**
     * `本表`中的`多态类型键`
     * 默认空, 表示不启用多态
     * @return `本表`中的`多态类型键`
     */
    String localModelMorphKey() default "";

    /**
     * `本表`中的`多态类型键`的值
     * 默认值为`父表`的表名
     * localModelMorphKey 为空时, 表示不启用多态
     * @return `本表`中的`多态类型键`的值
     */
    String localModelMorphValue() default "";
}
