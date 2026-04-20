package gaarason.database.spring.boot.starter.aop;

import gaarason.database.connection.GaarasonDataSourceContext;
import gaarason.database.exception.ProceedingInvocationException;
import gaarason.database.lang.Nullable;
import gaarason.database.spring.boot.starter.annotation.GaarasonDatabase;
import gaarason.database.spring.boot.starter.annotation.GaarasonDataSourceGroup;
import gaarason.database.spring.boot.starter.annotation.GaarasonTable;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;

import java.lang.reflect.Method;

/**
 * 数据源组 / Database / Table 三维度路由注解的 AOP 切面.
 * <p>
 * 拦截 {@link GaarasonDataSourceGroup}、{@link GaarasonDatabase}、{@link GaarasonTable} 标注的方法或类，
 * 自动入栈与恢复线程上下文；{@code spel=true} 时对 {@code value()} 做 SpEL 求值.
 *
 * @author xt
 */
@Aspect
public class GaarasonDataSourceAspect {

    private final GaarasonRoutingSpelEvaluator spelEvaluator;

    public GaarasonDataSourceAspect(ConfigurableBeanFactory beanFactory) {
        this.spelEvaluator = new GaarasonRoutingSpelEvaluator(beanFactory);
    }

    @Pointcut("@annotation(gaarason.database.spring.boot.starter.annotation.GaarasonDataSourceGroup)")
    public void methodAnnotationDataSourceGroup() {
    }

    @Pointcut("@within(gaarason.database.spring.boot.starter.annotation.GaarasonDataSourceGroup)")
    public void classAnnotationDataSourceGroup() {
    }

    @Pointcut("@annotation(gaarason.database.spring.boot.starter.annotation.GaarasonDatabase)")
    public void methodAnnotationDatabase() {
    }

    @Pointcut("@within(gaarason.database.spring.boot.starter.annotation.GaarasonDatabase)")
    public void classAnnotationDatabase() {
    }

    @Pointcut("@annotation(gaarason.database.spring.boot.starter.annotation.GaarasonTable)")
    public void methodAnnotationTable() {
    }

    @Pointcut("@within(gaarason.database.spring.boot.starter.annotation.GaarasonTable)")
    public void classAnnotationTable() {
    }

    /**
     * 环绕通知：方法级或类级路由注解
     *
     * @param point 连接点
     * @return 原方法返回值
     * @throws Throwable 原方法抛出的异常
     */
    @Around("methodAnnotationDataSourceGroup() || classAnnotationDataSourceGroup() || methodAnnotationDatabase() "
        + "|| classAnnotationDatabase() || methodAnnotationTable() || classAnnotationTable()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        Object[] args = point.getArgs();
        Object target = point.getTarget();

        // 解析注解
        GaarasonDataSourceGroup dataSourceGroup = resolveAnnotation(point, GaarasonDataSourceGroup.class);
        GaarasonDatabase database = resolveAnnotation(point, GaarasonDatabase.class);
        GaarasonTable table = resolveAnnotation(point, GaarasonTable.class);

        // 解析路由值
        String groupKey = dataSourceGroup == null ? null : resolveRoutingValue(
            dataSourceGroup.spel(), dataSourceGroup.value(), target, method, args, "GaarasonDataSourceGroup");
        String databaseKey = database == null ? null : resolveRoutingValue(
            database.spel(), database.value(), target, method, args, "GaarasonDatabase");
        String tableKey = table == null ? null : resolveRoutingValue(
            table.spel(), table.value(), target, method, args, "GaarasonTable");

        // 执行带有组、数据库、表路由的调用
        try {
            return invokeWithRouting(point, groupKey, databaseKey, tableKey);
        } catch (ProceedingInvocationException e) {
            throw e.getCause();
        }
    }

    /**
     * 执行带有组、数据库、表路由的调用
     * @param point 连接点
     * @param groupKey 数据源组键
     * @param databaseKey 数据库键
     * @param tableKey 表路由键(注解 {@code value} 或 SpEL 结果,与 {@link GaarasonDataSourceContext#executeTable} 入参一致)
     * @return 原方法返回值
     */
    private Object invokeWithRouting(ProceedingJoinPoint point, @Nullable String groupKey, @Nullable String databaseKey,
        @Nullable String tableKey) {
        if (groupKey != null) {
            return GaarasonDataSourceContext.executeDataSourceGroup(groupKey,
                () -> invokeWithRoutingWithoutGroup(point, databaseKey, tableKey));
        }
        return invokeWithRoutingWithoutGroup(point, databaseKey, tableKey);
    }

    /**
     * 执行带有数据库、表路由的调用
     * @param point 连接点
     * @param databaseKey 数据库键
     * @param tableKey 表路由键
     * @return 原方法返回值
     */
    private Object invokeWithRoutingWithoutGroup(ProceedingJoinPoint point, @Nullable String databaseKey,
        @Nullable String tableKey) {
        if (databaseKey != null) {
            return GaarasonDataSourceContext.executeDatabase(databaseKey,
                () -> invokeWithRoutingWithoutGroupAndDatabase(point, tableKey));
        }
        return invokeWithRoutingWithoutGroupAndDatabase(point, tableKey);
    }

    /**
     * 执行仅带有表路由的调用
     * @param point 连接点
     * @param tableKey 表路由键
     * @return 原方法返回值
     */
    private Object invokeWithRoutingWithoutGroupAndDatabase(ProceedingJoinPoint point, @Nullable String tableKey) {
        if (tableKey != null) {
            return GaarasonDataSourceContext.executeTable(tableKey, () -> proceed(point));
        }
        return proceed(point);
    }

    /**
     * 执行原方法.
     * @param point 连接点
     * @return 原方法返回值
     * @throws ProceedingInvocationException 仅用于携带 {@link ProceedingJoinPoint#proceed()} 的受检异常,由 {@link #around} 解包为 {@link Throwable#getCause()}
     */
    private Object proceed(ProceedingJoinPoint point) {
        try {
            return point.proceed();
        } catch (Throwable throwable) {
            throw new ProceedingInvocationException(throwable);
        }
    }

    /**
     * 解析路由值
     * @param spel 是否使用 SpEL
     * @param value 值
     * @param target 目标对象
     * @param method 方法
     * @param args 参数
     * @param annotationLabel 注解标签
     * @return 解析后的值
     */
    private String resolveRoutingValue(boolean spel, String value, Object target, Method method, Object[] args,
        String annotationLabel) {
        if (!spel) {
            return value;
        }
        return spelEvaluator.evaluateRequiredString(value, target, method, args, annotationLabel);
    }

    /**
     * 解析注解，方法级优先于类级
     */
    @Nullable
    private <A extends java.lang.annotation.Annotation> A resolveAnnotation(ProceedingJoinPoint point,
        Class<A> annotationClass) {
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        A annotation = method.getAnnotation(annotationClass);
        if (annotation != null) {
            return annotation;
        }
        return point.getTarget().getClass().getAnnotation(annotationClass);
    }
}
