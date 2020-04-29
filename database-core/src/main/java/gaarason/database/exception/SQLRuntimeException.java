package gaarason.database.exception;

import gaarason.database.exception.base.BaseException;

import java.util.Collection;

public class SQLRuntimeException extends BaseException {

    public SQLRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }
    public SQLRuntimeException(String sql, Collection<String> parameters, String message, Throwable cause) {
        super(message + " where perform : " +String.format(sql.replace(" ? ", "\"%s\""), parameters.toArray()), cause);
    }
}
