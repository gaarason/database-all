package gaarason.database.exception;

import gaarason.database.exception.base.BaseException;

/**
 * 新增失败
 * @author xt
 */
public class InsertNotSuccessException extends BaseException {

    public InsertNotSuccessException() {
        super();
    }

    public InsertNotSuccessException(String message, Throwable cause) {
        super(message, cause);
    }

}
