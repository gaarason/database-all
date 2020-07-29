package gaarason.database.eloquent.annotations;

import gaarason.database.eloquent.Model;

import java.lang.annotation.*;

@Documented
@Inherited
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface HasMany {

    /**
     * 关联model
     * @return
     */
    Class<? extends Model<?, ?>> targetModel();

    /**
     * targetModel的关联id, 一般为targetModel外键
     * @return
     */
    String foreignKey();

    /**
     * 一般为model主键
     * @return
     */
    String localKey() default "";
}
