package gaarason.database.annotation.conversion;

import gaarason.database.config.ConversionConfig;
import gaarason.database.contract.support.FieldConversion;
import gaarason.database.core.Container;
import gaarason.database.lang.Nullable;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 序列化与反序列化
 */
public class DefaultConversion extends Container.SimpleKeeper implements FieldConversion.Default {

    public DefaultConversion(Container container) {
        super(container);
    }

    @Nullable
    @Override
    public Object serialize(Field field, @Nullable Object fieldValue) {
        return fieldValue;
    }

    @Nullable
    @Override
    public Object acquisition(Field field, ResultSet resultSet, String column) throws SQLException {
        return getContainer().getBean(ConversionConfig.class).getValueFromJdbcResultSet(field, resultSet, column);
    }

    @Nullable
    @Override
    public Object deserialize(Field field, @Nullable Object originalValue) {
        return getContainer().getBean(ConversionConfig.class).castNullable(originalValue, field.getType());
    }
}