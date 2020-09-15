package gaarason.database.exception;

import gaarason.database.exception.base.BaseException;

public class InvalidEntityException extends BaseException {

    public InvalidEntityException(String message) {
        super(message);
    }

}
