package gaarason.database.exception;

import gaarason.database.exception.base.BaseException;

/**
 * 序列化失败
 * @author xt
 */
public class SerializeException extends BaseException {
    public SerializeException(String message) {
        super(message);
    }

    public SerializeException(String message, Throwable e) {
        super(message, e);
    }

    public SerializeException(Throwable e) {
        super(e);
    }
}
