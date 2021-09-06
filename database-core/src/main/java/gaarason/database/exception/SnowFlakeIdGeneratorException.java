package gaarason.database.exception;

import gaarason.database.exception.base.BaseException;

/**
 * 雪花算法异常
 * @author xt
 */
public class SnowFlakeIdGeneratorException extends BaseException {

    public SnowFlakeIdGeneratorException(String message) {
        super(message);
    }
}
