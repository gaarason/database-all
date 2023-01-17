package gaarason.database.exception;

import gaarason.database.appointment.EntityUseType;
import gaarason.database.exception.base.BaseException;
import gaarason.database.lang.Nullable;

import java.lang.reflect.Field;

/**
 * 字段无效
 * @author xt
 */
public class FieldInvalidException extends BaseException {

    public FieldInvalidException() {
        super();
    }

    public FieldInvalidException(Field field, @Nullable Object value, EntityUseType type) {
        super("The value [" + value + "] of field [" + field.getName() + "] is invalid in " + type + " scenario");
    }
}
