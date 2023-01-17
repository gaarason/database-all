package gaarason.database.generator.exception;

import gaarason.database.exception.base.BaseException;

/**
 * 生成代码时的异常
 * @author xt
 */
public class GeneratorException extends BaseException {
    public GeneratorException(String message) {
        super(message);
    }

    public GeneratorException(Throwable e) {
        super(e);
    }
}
