package gaarason.database.exception;

import gaarason.database.contract.eloquent.Model;
import gaarason.database.exception.base.BaseException;

import java.util.List;

/**
 * model创建失败
 * @author xt
 */
public class ModelNewInstanceException extends BaseException {

    public ModelNewInstanceException(Class<?> modelClass, String message, Throwable e) {
        super("Error instantiating object[" + modelClass + "] with message : " + message, e);
    }

    public ModelNewInstanceException(String message, Throwable e) {
        super(message, e);
    }

    public ModelNewInstanceException(Class<? extends Model<?, ?, ?>> modelClass, List<Throwable> throwableList) {
        super("Error instantiating object[" + modelClass + "] with message : "
            + throwableList.get(throwableList.size() - 1).getMessage(), throwableList.get(throwableList.size() - 1));
    }
}
