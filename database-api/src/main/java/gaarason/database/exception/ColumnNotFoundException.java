package gaarason.database.exception;

import gaarason.database.exception.base.BaseException;

/**
 * 类中不存在 数据库字段所对应的属性
 * @author xt
 */
public class ColumnNotFoundException extends BaseException {
    public ColumnNotFoundException(String message) {
        super(message);
    }
}
