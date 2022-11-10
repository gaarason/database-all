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
public class EnumStringConversion implements FieldConversion.EnumString {

    @Nullable
    @Override
    public String serialize(Field field, @Nullable Enum<?> fieldValue) {
        Enum<?> e = ObjectUtils.typeCastNullable(fieldValue);
        return ObjectUtils.isEmpty(e) ? "" : e.name();
    }

    @Nullable
    @Override
    public String acquisition(Field field, ResultSet resultSet, String columnName) throws SQLException {
        return resultSet.getString(columnName);
    }

    @Nullable
    @Override
    public Enum<?> deserialize(Field field, @Nullable String originalValue) {
        if(originalValue == null){
            return null;
        }
        Type type = field.getGenericType();
        Class<? extends Enum<?>> eClass = ObjectUtils.typeCast(ClassUtils.forName(type.getTypeName()));
        for (Enum<?> anEnum : eClass.getEnumConstants()) {
            if(anEnum.name().equals(originalValue)){
                return anEnum;
            }
        }
        return null;
    }
}