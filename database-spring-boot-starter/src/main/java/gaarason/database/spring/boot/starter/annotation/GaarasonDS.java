package gaarason.database.spring.boot.starter.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 数据源组切换注解
 * <p>
 * 可标注在方法或类上, 方法级别优先于类级别.
 * 使用后, 被标注的方法/类内所有数据库操作将路由到指定的数据源组.
 * <pre>{@code
 * @GaarasonDS("order")
 * public void processOrder() {
 *     orderModel.newQuery().where("id", 1).first();
 * }
 * }</pre>
 * @author xt
 * @see gaarason.database.connection.GaarasonDataSourceContext
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface GaarasonDS {

    /**
     * 数据源组名
     * @return 数据源组名
     */
    String value();
}
