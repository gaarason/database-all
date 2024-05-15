package gaarason.database.contract.support;

import gaarason.database.lang.Nullable;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 字段的类型转化
 * 序列化/反序列化
 * @param <F> 属性的类型
 * @param <D> 对应的jdbc可使用的类型
 */
public interface FieldConversion<F, D> {

    /**
     * 序列化
     * 将当前实体的属性的类型, 转化为jdbc可识别的类型
     * 场景 : 当前实体的属性, 以指定的方式拼接到sql (事实上是使用的sql绑定参数的方式).
     * eg: return String.valueOf(originalValue)
     * @param field 实体的属性
     * @param fieldValue 字段的原始值
     * @return 序列化后的值, 并非一定是字符串类型, 只要是jdbc可使用的即可
     */
    @Nullable
    D serialize(Field field, @Nullable F fieldValue);

    /**
     * 数据库结果集获取
     * 当从数据库中查询出结果后, 立即的获取结果集, 赋值到 record 的 metadataMap, 以便将数据库连接快速的放回池中.
     * 场景 : 这种情况会出现在查询结果后
     * eg: return resultSet.getObject(columnName)
     * @param field 实体的属性
     * @param resultSet 数据库结果集
     * @param columnName 当前列名
     * @return 反序列化后的值
     * @throws SQLException 数据库异常
     */
    @Nullable
    D acquisition(Field field, ResultSet resultSet, String columnName) throws SQLException;

    /**
     * 从任意的结果进行反序列化, 以便赋值到实体属性
     * 这种情况会出现在 Record.toObject()/Record.toObject(SomeEntity.class) 等方式下, 往往都是已经完成了"数据库结果集获取"之后的操作.
     * eg: return String.valueOf(originalValue)
     * @param field 实体的属性
     * @param originalValue 字段的原始值
     * @return 反序列化后的值, 可赋值到实体属性
     */
    @Nullable
    F deserialize(Field field, @Nullable D originalValue);

    /**
     * 默认值
     * 在运行时, 会替换
     */
    interface Default extends FieldConversion<Object, Object> {

    }

    /**
     * Json序列化
     * 实现依赖于jackson
     * 需要手动引入 com.fasterxml.jackson.core: jackson-databind
     * 需要手动引入 com.fasterxml.jackson.datatype: jackson-datatype-jsr310
     * 数据库列一般使用 varchar
     */
    interface Json extends FieldConversion<Object, String> {

    }

    /**
     * 枚举序列化, 使用枚举类的次序
     * 数据库列一般使用 int
     */
    interface EnumInteger extends FieldConversion<Enum<?>, Integer> {

    }

    /**
     * 枚举序列化, 使用枚举类的名称
     * 数据库列一般使用 varchar
     */
    interface EnumString extends FieldConversion<Enum<?>, String> {

    }

    /**
     * 按位序列化
     * eg : 将3个元素的集合 [0,1,2] 序列化为 (1 << 0) | (1 << 1) | (1 << 2) ,即单个数字 7
     * 数据库列一般使用 int, bigint ...
     */
    interface Bit extends FieldConversion<Object, Object> {

    }
}
