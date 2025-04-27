package gaarason.database.contract.builder;

import gaarason.database.contract.eloquent.Builder;
import gaarason.database.lang.Nullable;

import java.util.Collection;
import java.util.Map;

/**
 * 值
 * @param <T>
 * @param <K>
 * @author xt
 */
public interface Value<B extends Builder<B, T, K>, T, K> {

    /**
     * 插入数据使用
     * 需要手动调用 column() 方法
     * @param values 值列表
     * @return 查询构造器
     */
    B value(@Nullable Collection<?> values);

    /**
     * 插入数据使用
     * 已自动调用 column() 方法
     * @param entityMap 值列表map
     * @return 查询构造器
     */
    B value(@Nullable Map<String, Object> entityMap);

    /**
     * 插入数据使用
     * 已自动调用 column() 方法
     * @param anyEntity 实体
     * @return 查询构造器
     */
    B value(Object anyEntity);

    /**
     * 批量插入数据使用
     * @param entitiesOrMapsOrLists 多个实体 or 多个值MAP or多个值列表
     * @return 查询构造器
     */
    B values(@Nullable Collection<?> entitiesOrMapsOrLists);

}
