package gaarason.database.contracts.builder;

import gaarason.database.query.Builder;

import java.util.List;

/**
 * 值
 * @param <T>
 * @param <K>
 */
public interface Value<T, K> {

    /**
     * 插入数据使用
     * @param valueList 值列表
     * @return 查询构造器
     */
    Builder<T, K> value(List<String> valueList);

    /**
     * 批量插入数据使用
     * @param valueList 值列表的列表
     * @return 查询构造器
     */
    Builder<T, K> valueList(List<List<String>> valueList);

}
