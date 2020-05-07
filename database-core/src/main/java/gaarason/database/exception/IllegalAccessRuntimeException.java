package gaarason.database.exception;

import gaarason.database.exception.base.BaseException;

/**
 * 反射后的访问异常
 */
public class IllegalAccessRuntimeException extends BaseException {
    public IllegalAccessRuntimeException(Throwable cause) {
        super(cause);
    }
}
