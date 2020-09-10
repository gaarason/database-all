package gaarason.database.exception;

import gaarason.database.exception.base.BaseException;

/**
 * 查询不到结果
 */
public class EntityNotFoundException extends BaseException {
    public EntityNotFoundException(String message) {
        super(message);
    }

}
