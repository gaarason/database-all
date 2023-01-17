package gaarason.database.exception;

import gaarason.database.exception.base.BaseException;

/**
 * 链接关闭异常
 * @author xt
 */
public class ConnectionCloseException extends BaseException {

    public ConnectionCloseException(String message, Throwable e) {
        super(message, e);
    }
}
