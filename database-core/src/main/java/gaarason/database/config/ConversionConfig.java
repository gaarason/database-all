package gaarason.database.config;

import gaarason.database.core.lang.Nullable;
import gaarason.database.exception.TypeCastException;
import gaarason.database.provider.ModelShadowProvider;
import gaarason.database.util.ConverterUtils;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 类型转化
 * @author xt
 * @since 2021/11/30 5:00 下午
 */
public interface ConversionConfig {

    /**
     * 对象转指定类型
     * @param obj 对象
     * @param clz 类型
     * @return 对应类型的数据
     * @throws TypeCastException 类型转化失败
     * @see gaarason.database.eloquent.appointment.FinalVariable ALLOW_FIELD_TYPES
     */
    @Nullable
    default <R> R castNullable(@Nullable final Object obj, final Class<R> clz) throws TypeCastException {
        return ConverterUtils.castNullable(obj, clz);
    }

    /**
     * 对象转指定类型
     * @param obj 对象
     * @param clz 类型
     * @return 对应类型的数据
     * @throws TypeCastException 类型转化失败
     * @see gaarason.database.eloquent.appointment.FinalVariable ALLOW_FIELD_TYPES
     */
    default <R> R cast(final Object obj, final Class<R> clz) throws TypeCastException {
        return ConverterUtils.cast(obj, clz);
    }

    /**
     * 获取指定类型的默认值
     * @param clz 类型
     * @return 默认值
     * @throws TypeCastException 类型转换失败
     * @see gaarason.database.eloquent.appointment.FinalVariable ALLOW_FIELD_TYPES
     */
    @Nullable
    default <R> R getDefaultValueByJavaType(Class<R> clz) throws TypeCastException {
        return ConverterUtils.getDefaultValueByJavaType(clz);
    }

    /**
     * 根据java类型，获取jdbc中的数据结果
     * @param fieldInfo FieldInfo
     * @param resultSet 结果集
     * @param column    列名
     * @return 值
     * @throws SQLException 数据库异常
     * @see gaarason.database.eloquent.appointment.FinalVariable ALLOW_FIELD_TYPES
     */
    @Nullable
    default Object getValueFromJdbcResultSet(@Nullable ModelShadowProvider.FieldInfo fieldInfo, ResultSet resultSet,
        String column) throws SQLException {
        return ConverterUtils.getValueFromJdbcResultSet(fieldInfo, resultSet, column);
    }
}
