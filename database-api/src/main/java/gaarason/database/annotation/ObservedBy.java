package gaarason.database.annotation;

import gaarason.database.contract.model.Event;

import java.lang.annotation.*;

/**
 * 观察者注册
 * @author xt
 */
@Documented
@Inherited
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ObservedBy {

    Class<? extends Event<?, ?, ?>>[] value() default {};
}
