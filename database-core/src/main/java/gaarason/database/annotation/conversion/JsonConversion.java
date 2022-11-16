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
    public String serialize(Field field, @Nullable Object fieldValue) {
        boolean collection = ObjectUtils.isCollection(field.getType());
        if (collection && ObjectUtils.isEmpty(fieldValue)) {
            return "[]";
        }
        return JsonUtils.objectToJson(fieldValue);
    }

    @Nullable
    @Override
    public String acquisition(Field field, ResultSet resultSet, String columnName) throws SQLException {
        return resultSet.getString(columnName);
    }

    @Nullable
    @Override
    public Object deserialize(Field field, @Nullable String originalValue) {
        Type type = field.getGenericType();
        return JsonUtils.jsonToObject(originalValue, type);
    }
}