package gaarason.database.exception;

import gaarason.database.exception.base.BaseException;

/**
 * 参数异常
 */
public class AbnormalParameterException extends BaseException {

    public AbnormalParameterException(String message) {
        super(message);
    }
}
