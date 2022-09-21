package gaarason.database.contract.eloquent;

import gaarason.database.contract.record.CollectionOperationLambda;
import gaarason.database.contract.record.FriendlyList;
import gaarason.database.contract.record.RelationshipListLambda;
import gaarason.database.core.Container;

import java.util.*;

/**
 * 结果集集合
 * @author xt
 */
public interface RecordList<T, K> extends FriendlyList<T, K>,
    RelationshipListLambda<T, K>, List<Record<T, K>>,
    CollectionOperationLambda<T, K>, Container.Keeper, RandomAccess, Cloneable {

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
}
