package gaarason.database.exception;

import gaarason.database.exception.base.BaseException;

import java.util.Collection;

/**
 * sql异常
 * @author xt
 */
public class SQLRuntimeException extends BaseException {

    public SQLRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public SQLRuntimeException(String sql, Collection<String> parameters, String message, String symbol, Throwable cause) {
        super("message : [" + message + "] sql : [" + String.format(sql.replace(" ? ", symbol + "%s" + symbol),
            parameters.toArray()) + "]", cause);
    }
}
