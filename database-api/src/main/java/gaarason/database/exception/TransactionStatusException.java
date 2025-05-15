package gaarason.database.exception;

import gaarason.database.exception.base.BaseException;

/**
 * 事务状态异常
 * @author xt
 */
public class TransactionStatusException extends BaseException {

    public TransactionStatusException() {
        super();
    }

    public TransactionStatusException(String message) {
        super(message);
    }
}
