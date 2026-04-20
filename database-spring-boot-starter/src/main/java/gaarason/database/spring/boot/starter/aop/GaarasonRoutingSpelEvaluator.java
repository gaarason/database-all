package gaarason.database.spring.boot.starter.aop;

import gaarason.database.lang.Nullable;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import java.lang.reflect.Method;

/**
 * 路由注解 {@code spel=true} 时，将 {@code value()} 按 SpEL 在调用点求值为路由字符串.
 * @author xt
 */
final class GaarasonRoutingSpelEvaluator {

    private static final ParameterNameDiscoverer PARAMETER_NAME_DISCOVERER = new DefaultParameterNameDiscoverer();

    private final ExpressionParser expressionParser = new SpelExpressionParser();

    private final ConfigurableBeanFactory beanFactory;

    GaarasonRoutingSpelEvaluator(ConfigurableBeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    /**
     * 将表达式求值为非空字符串；{@code null} 或非字符串（除常见标量外）视为错误.
     * @param expressionString SpEL 表达式原文（整段）
     * @param target           被拦截的 Spring Bean
     * @param method           被调方法
     * @param args             方法实参
     * @param annotationLabel  用于异常信息，如 GaarasonTable
     * @return 路由字符串，非 {@code null}
     */
    String evaluateRequiredString(String expressionString, Object target, Method method, Object[] args,
        String annotationLabel) {
        Object raw = evaluate(expressionString, target, method, args);
        if (raw == null) {
            throw new IllegalArgumentException(
                "[" + annotationLabel + "] SpEL 求值为 null，表达式: [" + expressionString + "]");
        }
        if (raw instanceof String) {
            return (String) raw;
        }
        if (raw instanceof CharSequence) {
            return raw.toString();
        }
        if (raw instanceof Number || raw instanceof Boolean) {
            return String.valueOf(raw);
        }
        throw new IllegalArgumentException(
            "[" + annotationLabel + "] SpEL 求值类型不支持: " + raw.getClass().getName() + "，表达式: [" + expressionString
                + "]，请返回 String 或数字/布尔");
    }

    /**
     * 求值
     * @param expressionString SpEL 表达式原文（整段）
     * @param target 被拦截的 Spring Bean
     * @param method 被调方法
     * @param args 方法实参
     * @return 求值结果
     */
    @Nullable
    private Object evaluate(String expressionString, Object target, Method method, Object[] args) {
        Expression expression = expressionParser.parseExpression(expressionString);
        MethodBasedEvaluationContext context =
            new MethodBasedEvaluationContext(target, method, args, PARAMETER_NAME_DISCOVERER);
        context.setBeanResolver(new BeanFactoryResolver(beanFactory));
        return expression.getValue(context);
    }
}
