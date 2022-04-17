package gaarason.database.exception;

import gaarason.database.exception.base.BaseException;

/**
 * 无效的实体属性
 * @author xt
 */
public class EntityAttributeInvalidException extends BaseException {

    public EntityAttributeInvalidException(String fieldName, Class<?> entity) {
        super("There is no valid attribute [" + fieldName + "] in the entity [" + entity + "].");
    }
}
