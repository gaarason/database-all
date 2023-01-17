package gaarason.database.annotation;

import java.lang.annotation.*;

/**
 * 数据表
 * @author xt
 */
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
