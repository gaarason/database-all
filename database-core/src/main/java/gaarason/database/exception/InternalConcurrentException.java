package gaarason.database.exception;

/**
 * 出现此错误, 一般是高并发下的线程同步问题
 */
public class InternalConcurrentException extends RuntimeException {

    public InternalConcurrentException(String message) {
        super(message);
    }

    public InternalConcurrentException(String message, Throwable cause) {
        super(message, cause);
    }

}
