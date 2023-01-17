package gaarason.database.exception;

import gaarason.database.exception.base.BaseException;

/**
 * 参数异常
 * @author xt
 */
public class AbnormalParameterException extends BaseException {

    public AbnormalParameterException() {
        super();
    }

    public AbnormalParameterException(String message) {
        super(message);
    }
}
