package gaarason.database.contract.eloquent;

import gaarason.database.contract.record.CollectionOperationLambda;
import gaarason.database.contract.record.FriendlyList;
import gaarason.database.contract.record.RelationshipListLambda;
import gaarason.database.contract.support.ExtendedSerializable;
import gaarason.database.core.Container;

import java.util.*;

/**
 * 结果集集合
 * @author xt
 */
public interface RecordList<T, K> extends FriendlyList<T, K>,
    RelationshipListLambda<T, K>, List<Record<T, K>>,
    CollectionOperationLambda<T, K>, Container.Keeper, RandomAccess, Cloneable, ExtendedSerializable {

    /**
     * 元数据
     * @return 元数据
     */
    List<Map<String, Object>> getOriginalMetadataMapList();

    /**
     * 原始sql
     * @return sql
     */
    String getOriginalSql();

    /**
     * 内存缓存
     * @return 内存缓存
     */
    HashMap<Object, Set<Object>> getCacheMap();

    /**
     * 内存缓存
     * @param cacheMap 内存缓存
     */
    void setCacheMap(HashMap<Object, Set<Object>> cacheMap);

    /**
     * 反序列化到指定结果集合
     * @param bytes 序列化byte[]
     * @param <M> 实体类型
     * @param <N> 主键类型
     * @return 结果集合对象
     */
    static <M, N> RecordList<M, N> deserialize(byte[] bytes) {
        return ExtendedSerializable.deserialize(bytes);
    }

    /**
     * 反序列化到指定结果集合
     * @param serializeStr 序列化String
     * @param <M> 实体类型
     * @param <N> 主键类型
     * @return 结果集合对象
     */
    static <M, N> RecordList<M, N> deserialize(String serializeStr) {
        return ExtendedSerializable.deserialize(serializeStr);
    }
}
