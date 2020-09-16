package gaarason.database.eloquent.annotation;

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
