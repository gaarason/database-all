package gaarason.database.annotation.conversion;

import gaarason.database.contract.support.FieldConversion;
import gaarason.database.lang.Nullable;
import gaarason.database.util.EnumUtils;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 序列化与反序列化
 */
public class EnumStringConversion implements FieldConversion.EnumString {

    @Nullable
    @Override
    public String serialize(Field field, @Nullable Enum<?> fieldValue) {
        return EnumUtils.toStr(fieldValue);
    }

    @Nullable
    @Override
    public String acquisition(Field field, ResultSet resultSet, String columnName) throws SQLException {
        return resultSet.getString(columnName);
    }

    @Nullable
    @Override
    public Enum<?> deserialize(Field field, @Nullable String originalValue) {
        return EnumUtils.toEnum(originalValue, field.getGenericType());
    }
}