package gaarason.database.exception;

import gaarason.database.exception.base.BaseException;

public class TypeNotSupportedException extends BaseException {

    public TypeNotSupportedException(String message) {
        super(message);
    }

    public TypeNotSupportedException(String message, Throwable e) {
        super(message, e);
    }

}
