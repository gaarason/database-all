package gaarason.database.exception;

import gaarason.database.exception.base.BaseException;

/**
 * 查询结果赋值到对象失败
 * @author xt
 */
public class EntityNewInstanceException extends BaseException {
    public EntityNewInstanceException(String message, Throwable e) {
        super(message, e);
    }
}
