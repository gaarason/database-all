package gaarason.database.eloquent.annotation;

import gaarason.database.eloquent.appointment.IdGeneratorType;

import java.lang.annotation.*;

/**
 * 主键
 * @author xt
 */
@Documented
@Inherited
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Primary {
    /**
     * 自增主键
     * @return 自增主键
     */
    boolean increment() default true;

    /**
     * id生成策略
     * @return id生成策略
     */
    IdGeneratorType idGenerator() default IdGeneratorType.AUTO;
}
