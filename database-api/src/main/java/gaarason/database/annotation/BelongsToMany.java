package gaarason.database.annotation;

import gaarason.database.contract.eloquent.Model;

import java.lang.annotation.*;

/**
 * 类属性必须是 复数
 * @author xt
 */
@Documented
@Inherited
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface BelongsToMany {

    /**
     * `关系表`model
     * @return `关系表`
     */
    Class<? extends Model<?, ?>> relationModel();

    /**
     * `本表`中的`关联键`, 默认值为`本表`的主键(`@Primary()`修饰的键)
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
     * `目标表`中的`关联键`, 默认值为`目标表`的主键(`@Primary()`修饰的键)
     * @return `目标表`中的`关联键`
     */
    String targetModelLocalKey() default "";
}
