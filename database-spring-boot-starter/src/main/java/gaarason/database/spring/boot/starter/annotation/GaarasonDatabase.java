package gaarason.database.spring.boot.starter.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Database 维度：同链接切换 catalog/schema.
 * <p>
 * {@link #value()} 作为数据库键参与 {@link gaarason.database.connection.GaarasonDataSourceContext#executeDatabase}
 * 的上下文切换，
 * 最终 JDBC 库键由 {@link gaarason.database.contract.routing.DynamicDatabaseRouting} 基于该单一维度解析.
 * <p>
 * 当 {@link #spel()} 为 {@code true} 时，{@link #value()} 整段作为 SpEL 在方法调用点求值（可用方法参数名、{@code @beanName} 等），
 * 结果须为 {@link String} 或可转为字符串的标量；为 {@code false} 时 {@link #value()} 为字面量.
 *
 * @author xt
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface GaarasonDatabase {

    /**
     * 逻辑数据库键，或 {@link #spel()} 为 true 时的 SpEL 表达式
     *
     * @return 数据库键 / 表达式
     */
    String value();

    /**
     * 是否将 {@link #value()} 按 SpEL 解析
     *
     * @return 默认 false（字面量）
     */
    boolean spel() default false;
}
