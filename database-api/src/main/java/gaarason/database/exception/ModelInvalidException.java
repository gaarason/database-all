package gaarason.database.exception;

import gaarason.database.exception.base.BaseException;

/**
 * 不合法的model
 * @author xt
 */
public class ModelInvalidException extends BaseException {

    public ModelInvalidException(Class<?> clazz) {
        super("Model class[" + clazz + "] have no information in the Shadow.");
    }

}
