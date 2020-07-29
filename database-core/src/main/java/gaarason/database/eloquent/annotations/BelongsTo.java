package gaarason.database.eloquent.annotations;

import gaarason.database.eloquent.Model;

import java.lang.annotation.*;

@Documented
@Inherited
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface BelongsTo {

    /**
     * 所属的上级model
     * @return
     */
    Class<? extends Model<?, ?>> parentModel();

    /**
     * 本model的外键(指向上级model)
     * @return
     */
    String foreignKey();

    /**
     * parentModel的关联id, 一般为parentModel主键
     * @return
     */
    String localKey() default "";

}
