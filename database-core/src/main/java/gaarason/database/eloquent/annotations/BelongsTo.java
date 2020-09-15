package gaarason.database.eloquent.annotations;

import gaarason.database.contract.eloquent.Model;

import java.lang.annotation.*;

/**
 * 类属性必须是 单数
 */
@Documented
@Inherited
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface BelongsTo {

    /**
     * `父表`model
     * @return
     */
    Class<? extends Model<?, ?>> parentModel();

    /**
     * `本表`中的`关联父表的外键`
     * @return
     */
    String localModelForeignKey();

    /**
     * `父表`中的`关联键`, 默认值为`父表`的主键(`@Primary()`修饰的键)
     * @return
     */
    String parentModelLocalKey() default "";

}
