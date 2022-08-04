package gaarason.database.contract.support;

import gaarason.database.exception.OperationNotSupportedException;
import gaarason.database.lang.Nullable;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 字段的类型转化
 * 序列化/反序列化
 */
public interface FieldConversion {

    /**
     * 序列化
     * 当前实体的属性, 以指定的方式拼接到sql (事实上是使用的sql绑定参数的方式).
     * eg: return String.valueOf(originalValue)
     * @param originalValue 字段的原始值
     * @return 序列化后的值
     */
    @Nullable
    Object serialize(@Nullable Object originalValue);

    /**
     * 从数据库结果集进行反序列化
     * 当从数据库中查询出结果后, 以指定的对象来赋值到当前实体的属性.
     * 这种情况会出现在查询结果后, 使用当前model中声明的实体类型(model定义时的实体类型(实体泛型)),
     * 立即的获取结果集, 以便将数据库连接快速的放回池中.
     * eg: return resultSet.getObject(columnName)
     * @param field 实体的属性
     * @param resultSet 数据库结果集
     * @param columnName 当前列名
     * @return 反序列化后的值
     * @throws SQLException 数据库异常
     */
    @Nullable
    Object deserialize(Field field, ResultSet resultSet, String columnName) throws SQLException;

    /**
     * 从任意的结果进行反序列化
     * 这种情况会出现在 Record.toObject()/Record.toObject(SomeEntity.class) 等方式下, 往往都是已经完成了"从数据库结果集进行反序列化"之后的操作.
     * 如果当前model中没有声明有效的实体属性, 那么默认的 originalValue = resultSet.getObject(columnName), 其类型依赖数据库驱动.
     * eg: return String.valueOf(originalValue)
     * @param field 实体的属性
     * @param originalValue 字段的原始值
     * @return 反序列化后的值
     */
    @Nullable
    Object deserialize(Field field, @Nullable Object originalValue);

    /**
     * 默认值
     * 在运行时, 会替换
     */
    class Default implements FieldConversion {

        @Override
        public Object serialize(@Nullable Object originalValue) {
            throw new OperationNotSupportedException();
        }

        @Override
        public Object deserialize(Field field, ResultSet resultSet, String column) throws SQLException {
            throw new OperationNotSupportedException();
        }

        @Override
        public Object deserialize(Field field, @Nullable Object originalValue) {
            throw new OperationNotSupportedException();
        }
    }
}
