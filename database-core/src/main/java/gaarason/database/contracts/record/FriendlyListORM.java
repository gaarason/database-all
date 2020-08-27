package gaarason.database.contracts.record;

import gaarason.database.contracts.function.FilterRecordAttribute;
import gaarason.database.eloquent.RecordList;

import java.util.List;
import java.util.Map;

/**
 * 结果友好转化
 * @param <T> 实体类
 * @param <K> 主键类型
 */
public interface FriendlyListORM<T, K> {

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
     * @return 对象列表
     */
    List<T> toObjectList(Map<String, RecordList<?, ?>> cacheRelationRecordList);

    /**
     * 转化为map list
     * @return mapList
     */
    List<Map<String, Object>> toMapList();

    /**
     * 过滤成list
     * @return 单个字段列表
     */
    List<Object> toList(FilterRecordAttribute<T, K> filterRecordAttribute);


    /**
     * 元数据转实体对象列表, 不体现关联关系
     * @param clazz 自定义实体对象
     * @param <V>   自定义实体对象
     * @return 实体对象列表
     */
    <V> List<V> toObjectList(Class<V> clazz);
}
