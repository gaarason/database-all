package gaarason.database.exception;

import gaarason.database.exception.base.BaseException;

/**
 * 时间转化错误
 * @author xt
 */
public class TimeConversionException extends BaseException {

    public TimeConversionException(String timeStr) {
        super("[" + timeStr + "] conversion failed.");
    }

    public TimeConversionException(String message, Throwable e) {
        super(message, e);
    }

    public TimeConversionException(Throwable e) {
        super(e);
    }

}
