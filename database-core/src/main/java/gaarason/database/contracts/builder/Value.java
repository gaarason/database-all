package gaarason.database.contracts.builder;

import gaarason.database.query.Builder;

import java.util.List;

/**
 * 值
 * @param <T>
 */
public interface Value<T> {

    /**
     * 插入数据使用
     * @param valueList 值列表
     * @return 查询构造器
     */
    Builder<T> value(List<String> valueList);

    /**
     * 批量插入数据使用
     * @param valueList 值列表的列表
     * @return 查询构造器
     */
    Builder<T> valueList(List<List<String>> valueList);

}
