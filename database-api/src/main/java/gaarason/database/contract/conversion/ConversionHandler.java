package gaarason.database.contract.conversion;

import gaarason.database.appointment.JdbcType;
import gaarason.database.lang.Nullable;

import java.lang.reflect.Field;
import java.sql.ResultSet;

public interface ConversionHandler<JAVA_TYPE> {

    /**
     * 是否支持序列化到数据库
     * @param entityClass 实体类
     * @param tableName 表名
     * @param field 字段对象
     * @param columnName 列名
     * @return 是否支持
     */
    boolean supportSerialize(Class<?> entityClass, String tableName, Field field, String columnName);

    /**
     * 是否支持反序列化到java对象
     * @param entityClass 实体类
     * @param tableName 表名
     * @param field 字段对象
     * @param columnName 列名
     * @return 是否支持
     */
    boolean supportDeserialize(Class<?> entityClass, String tableName, Field field, String columnName);

    /**
     * 反序列化到java对象
     * @param resultSet 结果集
     * @param columnName 列名
     * @return 反序列化后的结果
     */
    @Nullable
    JAVA_TYPE deserialize(ResultSet resultSet, String columnName);

    /**
     * 序列化到
     * @param fieldValue
     * @param jdbcType
     */
    Object serialize(@Nullable JAVA_TYPE fieldValue, JdbcType jdbcType);
}
