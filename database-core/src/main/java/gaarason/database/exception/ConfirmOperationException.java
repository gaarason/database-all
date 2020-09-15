package gaarason.database.exception;

import gaarason.database.exception.base.BaseException;

/**
 * 需要确认操作
 */
public class ConfirmOperationException extends BaseException {

    public ConfirmOperationException(String message) {
        super(message);
    }
}
