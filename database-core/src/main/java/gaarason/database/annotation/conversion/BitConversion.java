package gaarason.database.annotation.conversion;

import gaarason.database.contract.support.FieldConversion;
import gaarason.database.exception.InvalidEntityException;
import gaarason.database.lang.Nullable;
import gaarason.database.util.BitUtils;
import gaarason.database.util.JsonUtils;
import gaarason.database.util.ObjectUtils;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

/**
 * 位序列化与反序列化
 */
public class BitConversion implements FieldConversion.Bit {

    @Nullable
    @Override
    public Object serialize(Field field, @Nullable Object fieldValue) {
        boolean collection = ObjectUtils.isCollection(field.getType());
        if (collection) {
            return BitUtils.packs((Collection<Object>) fieldValue);
        }
        throw new InvalidEntityException("列[" +field.getName()+"] 应该为集合类型");
    }

    @Nullable
    @Override
    public Object acquisition(Field field, ResultSet resultSet, String columnName) throws SQLException {
        return resultSet.getString(columnName);
    }

    @Nullable
    @Override
    public Object deserialize(Field field, @Nullable Object originalValue) {
        List<Long> unpack = BitUtils.unpack(originalValue);
        return JsonUtils.ObjectToObject(unpack, field.getGenericType());
    }
}