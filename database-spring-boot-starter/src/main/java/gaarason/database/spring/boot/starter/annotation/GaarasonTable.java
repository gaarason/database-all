package gaarason.database.spring.boot.starter.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 动态切表注解(Table 维度).
 * <p>
 * {@link #value()} 为路由表达式或路由键, 由 {@link gaarason.database.contract.routing.DynamicTableRouting} 解析物理表名;
 * 与 {@link GaarasonDataSourceGroup}、{@link GaarasonDatabase} 可组合使用.
 * <p>
 * {@link #spel()} 为 {@code true} 时 {@link #value()} 按 SpEL 求值，规则同 {@link GaarasonDatabase#spel()}.
 *
 * @author xt
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface GaarasonTable {

    /**
     * 路由表达式/路由键，或 {@link #spel()} 为 true 时的 SpEL 表达式
     *
     * @return 路由表达式
     */
    String value();

    /**
     * 是否将 {@link #value()} 按 SpEL 解析
     *
     * @return 默认 false（字面量）
     */
    boolean spel() default false;
}

