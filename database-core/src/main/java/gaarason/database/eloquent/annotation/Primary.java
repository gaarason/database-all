package gaarason.database.eloquent.annotation;

import gaarason.database.eloquent.appointment.IdGeneratorType;

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

    /**
     * id生成策略
     * @return
     */
    IdGeneratorType idGenerator() default IdGeneratorType.AUTO;
}
