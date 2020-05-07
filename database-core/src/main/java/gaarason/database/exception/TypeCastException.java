package gaarason.database.exception;

import gaarason.database.exception.base.BaseException;

/**
 * 强制类型转化异常
 */
public class TypeCastException extends BaseException {

    public TypeCastException(String message, Throwable cause) {
        super(message, cause);
    }
}
