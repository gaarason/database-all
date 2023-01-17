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
public class EnumIntegerConversion implements FieldConversion.EnumInteger {

    @Nullable
    @Override
    public Integer serialize(Field field, @Nullable Enum<?> fieldValue) {
        return EnumUtils.toInt(fieldValue);
    }

    @Nullable
    @Override
    public Integer acquisition(Field field, ResultSet resultSet, String columnName) throws SQLException {
        return resultSet.getInt(columnName);
    }

    @Nullable
    @Override
    public Enum<?> deserialize(Field field, @Nullable Integer originalValue) {
        return EnumUtils.toEnum(originalValue, field.getGenericType());
    }
}