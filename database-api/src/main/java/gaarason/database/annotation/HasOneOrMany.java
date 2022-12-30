package gaarason.database.annotation;

import java.lang.annotation.*;

/**
 * 当前表的当前属性存在一个or多个下级关系
 * 数据库中表结构, 一般为下级表中存在 foreignKey 指向当前表的 id
 * 类属性 可是是单数 or 复数
 * @author xt
 */
@Documented
@Inherited
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface HasOneOrMany {

    /**
     * `子表`中的`关联本表的外键`
     * @return `子表`中的`关联本表的外键`
     */
    String sonModelForeignKey();

    /**
     * `本表`中的`关联键`
     * 默认值为`本表`的主键(`@Primary()`修饰的键)
     * @return `本表`中的`关联键`
     */
    String localModelLocalKey() default "";

    /**
     * `子表`中的`多态类型键`
     * 默认空, 表示不启用多态
     * @return `子表`中的`多态类型键`
     */
    String sonModelMorphKey() default "";

    /**
     * `子表`中的`多态类型键`的值
     * 默认值为`本表`的表名
     * sonModelMorphKey 为空时, 表示不启用多态
     * @return `子表`中的`多态类型键`的值
     */
    String sonModelMorphValue() default "";

}
