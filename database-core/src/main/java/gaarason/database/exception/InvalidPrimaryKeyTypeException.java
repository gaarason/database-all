package gaarason.database.exception;

import gaarason.database.exception.base.BaseException;

/**
 * 主键类型检测异常
 * 实体上的主键的类型与模型上的泛型类型 不一致
 * @author xt
 */
public class InvalidPrimaryKeyTypeException extends BaseException {

    public InvalidPrimaryKeyTypeException(String message) {
        super(message);
    }
}
