package gaarason.database.exception;

import gaarason.database.exception.base.BaseException;

/**
 * 不合法的实体
 * @author xt
 */
public class InvalidEntityException extends BaseException {

    public InvalidEntityException(String message) {
        super(message);
    }

}
