package gaarason.database.config;

import gaarason.database.appointment.FinalVariable;
import gaarason.database.exception.TypeCastException;
import gaarason.database.lang.Nullable;
import gaarason.database.provider.FieldInfo;

import java.lang.reflect.Field;
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
     * @see FinalVariable ALLOW_FIELD_TYPES
     */
    @Nullable
    <R> R castNullable(@Nullable final Object obj, final Class<R> clz) throws TypeCastException;

    /**
     * 对象转指定类型
     * @param obj 对象
     * @param clz 类型
     * @return 对应类型的数据
     * @throws TypeCastException 类型转化失败
     * @see FinalVariable ALLOW_FIELD_TYPES
     */
    <R> R cast(final Object obj, final Class<R> clz) throws TypeCastException;

    /**
     * 获取指定类型的默认值
     * @param clz 类型
     * @return 默认值
     * @throws TypeCastException 类型转换失败
     * @see FinalVariable ALLOW_FIELD_TYPES
     */
    @Nullable
    <R> R getDefaultValueByJavaType(Class<R> clz) throws TypeCastException;

    /**
     * 根据java类型，获取jdbc中的数据结果
     * @param field field
     * @param resultSet 结果集
     * @param column    列名
     * @return 值
     * @throws SQLException 数据库异常
     * @see FinalVariable ALLOW_FIELD_TYPES
     */
    @Nullable
    Object getValueFromJdbcResultSet(@Nullable Field field, ResultSet resultSet, String column) throws SQLException;


}
