package gaarason.database.exception;

import gaarason.database.exception.base.BaseException;

/**
 * 语法错误
 * @author xt
 */
public class GrammarException extends BaseException {

    public GrammarException(String message) {
        super(message);
    }

    public GrammarException(String message, Throwable e) {
        super(message, e);
    }

}
