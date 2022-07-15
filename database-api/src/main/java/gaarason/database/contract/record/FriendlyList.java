package gaarason.database.contract.record;

import gaarason.database.contract.eloquent.RecordList;
import gaarason.database.contract.function.FilterRecordAttributeFunctionalInterface;

import java.util.List;
import java.util.Map;

/**
 * 结果友好转化
 * @param <T> 实体类
 * @param <K> 主键类型
 * @author xt
 */
public interface FriendlyList<T, K> {

    /**
     * 转化为对象列表
     * @return 对象列表
     */
    List<T> toObjectList();

    /**
     * 转化为对象列表
     * @return 对象列表
     */
    List<T> toObjectListWithoutRelationship();

    /**
     * 转化为对象列表
     * @param cacheRelationRecordList 结果集缓存(用于优化递归算法)
     * @return 对象列表
     */
    List<T> toObjectList(Map<String, RecordList<?, ?>> cacheRelationRecordList);

    /**
     * 转化为map list
     * @return mapList
     */
    List<Map<String, Object>> toMapList();

    /**
     * 转化为 list map
     * @return listMap
     */
    Map<String, List<Object>> toListMap();

    /**
     * 过滤成list
     * @param filterRecordAttributeFunctionalInterface 结果集过滤
     * @param <V> 指定的响应类型
     * @return 单个字段列表
     */
    <V> List<V> toList(FilterRecordAttributeFunctionalInterface<T, K, V> filterRecordAttributeFunctionalInterface);

    /**
     * 过滤成list(取Record中的第一列)
     * @return 单个字段列表
     */
    List<Object> toOneColumnList();

    /**
     * 元数据转实体对象列表, 不体现关联关系
     * @param clazz 自定义实体对象
     * @param <V> 自定义实体对象
     * @return 实体对象列表
     */
    <V> List<V> toObjectList(Class<V> clazz);
}
