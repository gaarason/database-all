package gaarason.database.eloquent.annotations;

import java.lang.annotation.*;


@Documented
@Inherited
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Primary {
    /**
     * 自增主键
     * @return
     */
    boolean increment() default true;
}
