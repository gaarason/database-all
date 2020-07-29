package gaarason.database.eloquent.annotations;

import gaarason.database.eloquent.Model;

import java.lang.annotation.*;

@Documented
@Inherited
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface HasOne {

    /**
     * 关联model
     * @return
     */
    Class<? extends Model<?, ?>> targetModel();

    /**
     * model的关联id, 一般为model外键
     * @return
     */
    String foreignKey();

    /**
     * targetModel的关联id, 一般为targetModel主键
     * @return
     */
    String localKey() default "";

}
