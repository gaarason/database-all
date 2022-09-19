package gaarason.database.annotation.conversion;

import gaarason.database.contract.support.FieldConversion;
import gaarason.database.lang.Nullable;
import gaarason.database.util.JsonUtils;
import gaarason.database.util.ObjectUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 序列化与反序列化
 */
public class JsonConversion implements FieldConversion.Json {

    @Nullable
    @Override
    public Object serialize(Field field, @Nullable Object originalValue) {
        boolean collection = ObjectUtils.isCollection(field.getType());
        if(collection && ObjectUtils.isEmpty(originalValue)){
            return "[]";
        }
        return JsonUtils.objectToJson(originalValue);
    }

    @Nullable
    @Override
    public Object deserialize(Field field, ResultSet resultSet, String columnName) throws SQLException {
        return resultSet.getString(columnName);
    }

    @Nullable
    @Override
    public Object deserialize(Field field, @Nullable Object originalValue) {
        Type type = field.getGenericType();
        if(originalValue instanceof CharSequence){
            return JsonUtils.jsonToObject(String.valueOf(originalValue), type);
        }
        return JsonUtils.ObjectToObject(originalValue, type);
    }
}