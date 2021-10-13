package gaarason.database.exception;

import gaarason.database.exception.base.BaseException;

/**
 * 反射错误
 * @author xt
 */
public class ReflectionNotSupportedException extends BaseException {

    public ReflectionNotSupportedException(String message) {
        super(message);
    }

    public ReflectionNotSupportedException(String message, Throwable e) {
        super(message, e);
    }

    public ReflectionNotSupportedException(Throwable e) {
        super(e);
    }

}
