package gaarason.database.exception;

import gaarason.database.exception.base.BaseException;

/**
 * 对象创建失败
 * @author xt
 */
public class ObjectNewInstanceException extends BaseException {

    public ObjectNewInstanceException(Class<?> clazz, Throwable e) {
        super("Error instantiating object[" + clazz + "]", e);
    }

    public ObjectNewInstanceException(Class<?> clazz, String message, Throwable e) {
        super("Error instantiating object[" + clazz + "] with message : " + message, e);
    }
}
