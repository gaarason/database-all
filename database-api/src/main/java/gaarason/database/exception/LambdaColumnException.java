package gaarason.database.exception;

import gaarason.database.exception.base.BaseException;

/**
 * Lambda风格的列名解析失败
 * @author xt
 */
public class LambdaColumnException extends BaseException {
    public LambdaColumnException(Throwable e) {
        super(e);
    }
}
