package gaarason.database.exception;

import gaarason.database.exception.base.BaseException;

/**
 * 暂不支持的主键类型
 */
public class PrimaryKeyTypeNotSupportException extends BaseException {
    public PrimaryKeyTypeNotSupportException(String message) {
        super(message);
    }
}
