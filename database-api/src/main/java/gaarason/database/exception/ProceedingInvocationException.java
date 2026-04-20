package gaarason.database.exception;

import gaarason.database.exception.base.BaseException;

/**
 * 将受检异常包装为运行时异常,用于穿过未声明 {@code throws} 的函数式边界(如 {@link java.util.function.Supplier});
 * 仅作透传容器,捕获方应抛出 {@link #getCause()} 以恢复原始异常类型.
 *
 * @author xt
 */
public class ProceedingInvocationException extends BaseException {

    public ProceedingInvocationException(Throwable cause) {
        super(cause);
    }
}
