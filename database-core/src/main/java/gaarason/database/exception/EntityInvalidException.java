package gaarason.database.exception;

import gaarason.database.exception.base.BaseException;

/**
 * 不合法的实体
 * @author xt
 */
public class EntityInvalidException extends BaseException {

    public EntityInvalidException(Class<?> clazz){
        super("Entity class[" + clazz + "] have no information in the Shadow.");
    }

}
