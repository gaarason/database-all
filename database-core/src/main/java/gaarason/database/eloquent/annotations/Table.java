package gaarason.database.eloquent.annotations;

import java.lang.annotation.*;


@Documented
@Inherited
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Table {
    /**
     * 表名
     * @return
     */
    String name() default "";
}
