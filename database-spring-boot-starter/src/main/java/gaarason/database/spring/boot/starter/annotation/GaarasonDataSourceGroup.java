package gaarason.database.spring.boot.starter.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * DataSourceGroup 维度：数据源组切换.
 * <p>
 * 可标注在方法或类上，方法级别优先于类级别.
 * 使用后，被标注的方法/类内数据库操作将路由到指定数据源组.
 * {@link #value()} 写入逻辑组键；物理组名由 {@link gaarason.database.contract.routing.DynamicDataSourceGroupRouting}
 * 基于该单一维度解析.
 * <p>
 * {@link #spel()} 为 {@code true} 时 {@link #value()} 按 SpEL 求值，规则同 {@link GaarasonDatabase#spel()}.
 * <pre>{@code
 * @GaarasonDataSourceGroup("order")
 * public void processOrder() {
 *     orderModel.newQuery().where("id", 1).first();
 * }
 * }</pre>
 *
 * @author xt
 * @see gaarason.database.connection.GaarasonDataSourceContext
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface GaarasonDataSourceGroup {

    /**
     * 逻辑数据源组名，或 {@link #spel()} 为 true 时的 SpEL 表达式
     *
     * @return 数据源组名 / 表达式
     */
    String value();

    /**
     * 是否将 {@link #value()} 按 SpEL 解析
     *
     * @return 默认 false（字面量）
     */
    boolean spel() default false;
}
