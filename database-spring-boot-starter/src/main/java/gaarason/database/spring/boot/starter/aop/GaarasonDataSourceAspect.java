package gaarason.database.spring.boot.starter.aop;

import gaarason.database.connection.GaarasonDataSourceContext;
import gaarason.database.lang.Nullable;
import gaarason.database.spring.boot.starter.annotation.GaarasonDS;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Method;

/**
 * {@link GaarasonDS} 注解的 AOP 切面
 * <p>
 * 拦截 {@code @GaarasonDS} 注解标注的方法和类, 自动进行数据源组的切换与恢复.
 * 支持嵌套调用(栈式保存/恢复前值).
 * @author xt
 */
@Aspect
public class GaarasonDataSourceAspect {

    @Pointcut("@annotation(gaarason.database.spring.boot.starter.annotation.GaarasonDS)")
    public void methodAnnotation() {
    }

    @Pointcut("@within(gaarason.database.spring.boot.starter.annotation.GaarasonDS)")
    public void classAnnotation() {
    }

    /**
     * 环绕通知: 方法级或类级 @GaarasonDS
     * @param point 连接点
     * @return 原方法返回值
     * @throws Throwable 原方法抛出的异常
     */
    @Around("methodAnnotation() || classAnnotation()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        GaarasonDS ds = resolveAnnotation(point);
        if (ds == null) {
            return point.proceed();
        }
        GaarasonDataSourceContext.set(ds.value());
        try {
            return point.proceed();
        } finally {
            GaarasonDataSourceContext.clear();
        }
    }

    /**
     * 解析 @GaarasonDS 注解, 方法级优先于类级
     */
    @Nullable
    private GaarasonDS resolveAnnotation(ProceedingJoinPoint point) {
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        GaarasonDS ds = method.getAnnotation(GaarasonDS.class);
        if (ds != null) {
            return ds;
        }
        return point.getTarget().getClass().getAnnotation(GaarasonDS.class);
    }
}
