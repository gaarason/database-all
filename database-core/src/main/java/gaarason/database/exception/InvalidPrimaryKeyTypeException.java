package gaarason.database.exception;

import gaarason.database.exception.base.BaseException;

/**
 * 主键类型检测异常
 */
public class InvalidPrimaryKeyTypeException extends BaseException {
    public InvalidPrimaryKeyTypeException(String message) {
        super(message);
    }
}
