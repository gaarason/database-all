package gaarason.database.annotation;

import gaarason.database.contract.eloquent.Model;

import java.lang.annotation.*;

/**
 * 类属性必须是 复数
 * @author xt
 */
@Documented
@Inherited
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface BelongsToMany {

    /**
     * `关系表`model
     * @return `关系表`
     */
    Class<? extends Model<?, ?>> relationModel();

    /**
     * `本表`中的`关联键`
     * 默认值为`本表`的主键(`@Primary()`修饰的键)
     * @return `本表`中的`关联键`
     */
    String localModelLocalKey() default "";

    /**
     * `关系表`中的`关联本表的外键`
     * @return `关系表`中的`关联本表的外键`
     */
    String foreignKeyForLocalModel();

    /**
     * `关系表`中的`关联目标表的外键`
     * @return `关系表`中的`关联目标表的外键`
     */
    String foreignKeyForTargetModel();

    /**
     * `目标表`中的`关联键`
     * 默认值为`目标表`的主键(`@Primary()`修饰的键)
     * @return `目标表`中的`关联键`
     */
    String targetModelLocalKey() default "";

    /**
     * `关系表`中的`本表`的`多态类型键`
     * 默认空, 表示对于`本表`不启用多态
     * @return `关系表`中的`本表`的`多态类型键`
     */
    String morphKeyForLocalModel() default "";

    /**
     * `关系表`中的`本表`的`多态类型键`的值
     * 默认值为`本表`的表名
     * morphKeyForLocalModel 为空时, 表示对于`本表`不启用多态
     * @return `关系表`中的`本表`的`多态类型键`的值
     */
    String morphValueForLocalModel() default "";

    /**
     * `关系表`中的`目标表`的`多态类型键`
     * 默认空, 表示对于`目标表`不启用多态
     * @return `关系表`中的`目标表`的`多态类型键`
     */
    String morphKeyForTargetModel() default "";

    /**
     * `关系表`中的`目标表`的`多态类型键`的值
     * 默认值为`目标表`的表名
     * morphKeyForTargetModel 为空时, 表示对于`目标表`不启用多态
     * @return `关系表`中的`目标表`的`多态类型键`的值
     */
    String morphValueForTargetModel() default "";
}
