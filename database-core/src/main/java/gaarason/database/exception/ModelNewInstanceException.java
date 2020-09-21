package gaarason.database.exception;

import gaarason.database.contract.eloquent.Model;
import gaarason.database.exception.base.BaseException;

import java.util.List;

/**
 * model创建失败
 */
public class ModelNewInstanceException extends BaseException {

    protected List<Throwable> throwableList;

    public ModelNewInstanceException(Class<? extends Model<?, ?>> modelClass, String message, Throwable e) {
        super("Error instantiating model[" + modelClass + "] with message : " + message, e);
    }

    public ModelNewInstanceException(String message, Throwable e) {
        super(message, e);
    }

    public ModelNewInstanceException(Class<? extends Model<?, ?>> modelClass, List<Throwable> throwableList) {
        super("Error instantiating model[" + modelClass + "] with message : "
            + throwableList.get(throwableList.size() - 1).getMessage(), throwableList.get(throwableList.size() - 1));
        this.throwableList = throwableList;
    }
}
