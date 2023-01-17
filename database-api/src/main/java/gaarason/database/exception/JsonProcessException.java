package gaarason.database.exception;

import gaarason.database.exception.base.BaseException;

/**
 * json编码错误
 * @author xt
 */
public class JsonProcessException extends BaseException {

    public JsonProcessException(String json, Object typeDes, Throwable cause) {
        super("Failed to deserialize JSON[" + json + "] to object type[" + typeDes + "]", cause);
    }

    public JsonProcessException(Object obj, Throwable cause) {
        super("Failed to serialize OBJ[" + obj + "] to json", cause);
    }


}
