package gaarason.database.exception;

import gaarason.database.exception.base.BaseException;

/**
 * 克隆失败
 * @author xt
 */
public class CloneNotSupportedRuntimeException extends BaseException {

    public CloneNotSupportedRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

}
