package gaarason.database.exception;

import gaarason.database.exception.base.BaseException;

/**
 * 配置出错
 * @author xt
 */
public class InvalidConfigException extends BaseException {

    public InvalidConfigException(String message) {
        super(message);
    }

}
