package gaarason.database.exception;

import gaarason.database.exception.base.BaseException;

/**
 * 类加载失败
 * @author xt
 */
public class ClassNotFoundException extends BaseException {
    public ClassNotFoundException(String className) {
        super("Class find by name ["+className+"] fail.");
    }
}
