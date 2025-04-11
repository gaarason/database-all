package gaarason.database.contract.record;

import java.util.Map;

/**
 * 结果友好转化
 * @param <T> 实体类
 * @param <K> 主键类型
 * @author xt
 */
public interface Friendly<T, K> {

    /**
     * 将元数据map转化为普通map
     * @return 普通map
     */
    Map<String, Object> toMap();

    /**
     * 元数据转String
     * @return eg:age=16&name=alice&sex=
     */
    String toSearch();

    /**
     * 元数据转实体对象
     * @return 实体对象
     */
    T toObject();

    /**
     * 元数据转实体对象, 不体现关联关系
     * @return 实体对象
     */
    T toObjectWithoutRelationship();

    /**
     * 元数据转实体对象, 不体现关联关系
     * @param clazz 自定义实体对象
     * @param <V> 自定义实体对象
     * @return 实体对象
     */
    <V> V toObject(Class<V> clazz);

}
