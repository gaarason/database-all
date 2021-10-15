package gaarason.database.exception;

import gaarason.database.exception.base.BaseException;

/**
 * 类型错误
 * @author xt
 */
public class TypeNotSupportedException extends BaseException {

    public TypeNotSupportedException() {
        super();
    }

    public TypeNotSupportedException(String message) {
        super(message);
    }

    public TypeNotSupportedException(String message, Throwable e) {
        super(message, e);
    }

}
