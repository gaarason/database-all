package gaarason.database.contract.builder;

import gaarason.database.contract.eloquent.Builder;
import gaarason.database.lang.Nullable;

import java.util.Collection;
import java.util.Map;

/**
 * 数据
 * @param <T>
 * @param <K>
 * @author xt
 */
public interface Data<B extends Builder<B, T, K>, T, K> {

    /**
     * 数据更新
     * @param sqlPart sql片段 eg: age=15,name="dd"
     * @return 查询构造器
     */
    B dataRaw(@Nullable String sqlPart);

    /**
     * 数据更新
     * @param column 列名
     * @param value 值
     * @return 查询构造器
     */
    B data(String column, @Nullable Object value);

    /**
     * 数据更新(忽略值为null的情况)
     * @param column 列名
     * @param value 值
     * @return 查询构造器
     */
    B dataIgnoreNull(String column, @Nullable Object value);

    /**
     * 数据更新
     * @param anyEntity 任意实体对象
     * @return 查询构造器
     */
    B data(Object anyEntity);

    /**
     * 数据更新
     * @param map Map<String column, String value>
     * @return 查询构造器
     */
    B data(Map<String, Object> map);

    /**
     * 数据更新(忽略值为null的情况)
     * @param map Map<String column, String value>
     * @return 查询构造器
     */
    B dataIgnoreNull(@Nullable Map<String, Object> map);

    /**
     * 字段自增
     * @param column 列名
     * @param steps 步长
     * @return 查询构造器
     */
    B dataIncrement(String column, Object steps);

    /**
     * 字段自减
     * @param column 列名
     * @param steps 步长
     * @return 查询构造器
     */
    B dataDecrement(String column, Object steps);

    /**
     * 字段设定选项值
     * @param column 列名(位存储)
     * @param values 选项值集合(eg: 0,1,2,3)
     * @return 查询构造器
     */
    B dataBit(String column, Collection<Object> values);

    /**
     * 字段增定选项值
     * @param column 列名(位存储)
     * @param values 选项值集合(eg: 0,1,2,3)
     * @return 查询构造器
     */
    B dataBitIncrement(String column, Collection<Object> values);

    /**
     * 字段移除选项值
     * @param column 列名(位存储)
     * @param values 选项值集合(eg: 0,1,2,3)
     * @return 查询构造器
     */
    B dataBitDecrement(String column, Collection<Object> values);

}
