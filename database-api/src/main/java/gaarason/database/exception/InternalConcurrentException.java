package gaarason.database.exception;

import gaarason.database.exception.base.BaseException;

/**
 * 出现此错误, 一般是高并发下的线程同步问题
 * @author xt
 */
public class InternalConcurrentException extends BaseException {

    public InternalConcurrentException(String message) {
        super(message);
    }

    public InternalConcurrentException(String message, Throwable cause) {
        super(message, cause);
    }

}
