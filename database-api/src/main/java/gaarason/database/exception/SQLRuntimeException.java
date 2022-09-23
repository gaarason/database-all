package gaarason.database.exception;

import gaarason.database.exception.base.BaseException;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * sql异常
 * @see SQLIntegrityConstraintViolationException SQL完整性约束冲突异常(foreign key, primary key or unique key)
 * @author xt
 */
public class SQLRuntimeException extends BaseException {

    private static final Pattern COMPILE = Pattern.compile(" ? ",
        Pattern.LITERAL);

    /**
     * 构造函数
     * @param message 异常消息
     * @param cause 原异常
     */
    public SQLRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 构造函数
     * @param sql sql语句
     * @param parameters sql语句的绑定参数
     * @param message 异常消息
     * @param symbol sql语句的绑定参数的掩盖符号
     * @param cause 原异常
     */
    public SQLRuntimeException(String sql, Collection<?> parameters, String message, String symbol, Throwable cause) {
        super("message : [" + message + "] sql : [" + String.format(
            COMPILE.matcher(sql).replaceAll(Matcher.quoteReplacement(symbol + "%s" + symbol)),
            parameters.toArray()) + "]", cause);
    }

    /**
     * 构造函数
     * @param sql sql语句
     * @param message 异常消息
     * @param cause 原异常
     */
    public SQLRuntimeException(String sql, String message, Throwable cause) {
        super("message : [" + message + "] sql : [" + sql + "]", cause);
    }
}
