package gaarason.database.annotation;

import gaarason.database.contract.support.IdGenerator;

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
    Class<? extends IdGenerator> idGenerator() default IdGenerator.Auto.class;
}
