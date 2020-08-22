package gaarason.database.exception;

import gaarason.database.exception.base.BaseException;

/**
 * model创建失败
 */
public class ModelNewInstanceException extends BaseException {
    public ModelNewInstanceException(String message, Throwable e) {
        super(message, e);
    }
}
