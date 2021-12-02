package gaarason.database.contract.eloquent;

import gaarason.database.contract.record.CollectionOperation;
import gaarason.database.contract.record.FriendlyList;
import gaarason.database.contract.record.RelationshipList;
import gaarason.database.support.Column;

import java.io.Serializable;
import java.util.*;

/**
 * 结果集集合
 * @author xt
 */
public interface RecordList<T extends Serializable, K extends Serializable> extends FriendlyList<T, K>, RelationshipList<T, K>, List<Record<T, K>>,
    CollectionOperation<Record<T, K>>, RandomAccess, Cloneable {

    /**
     * 元数据
     * @return 元数据
     */
    List<Map<String, Column>> getOriginalMetadataMapList();

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
}
