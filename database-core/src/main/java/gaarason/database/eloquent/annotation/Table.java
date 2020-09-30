package gaarason.database.eloquent.annotation;

import java.lang.annotation.*;


@Documented
@Inherited
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Table {
    /**
     * 表名
     * @return 表名
     */
    String name() default "";
}
