package gaarason.database.annotation.conversion;

import gaarason.database.contract.support.FieldConversion;
import gaarason.database.lang.Nullable;
import gaarason.database.util.ClassUtils;
import gaarason.database.util.ObjectUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 序列化与反序列化
 */
public class EnumIntegerConversion implements FieldConversion.EnumInteger {

    @Nullable
    @Override
    public Integer serialize(Field field, @Nullable Enum<?> fieldValue) {
        Enum<?> e = ObjectUtils.typeCastNullable(fieldValue);
        return ObjectUtils.isEmpty(e) ? -1 : e.ordinal();
    }

    @Nullable
    @Override
    public Integer acquisition(Field field, ResultSet resultSet, String columnName) throws SQLException {
        return resultSet.getInt(columnName);
    }

    @Nullable
    @Override
    public Enum<?> deserialize(Field field, @Nullable Integer originalValue) {
        if (originalValue == null || originalValue == -1) {
            return null;
        }
        Type type = field.getGenericType();
        Class<? extends Enum<?>> eClass = ObjectUtils.typeCast(ClassUtils.forName(type.getTypeName()));
        for (Enum<?> anEnum : eClass.getEnumConstants()) {
            if (anEnum.ordinal() == originalValue) {
                return anEnum;
            }
        }
        return null;
    }
}