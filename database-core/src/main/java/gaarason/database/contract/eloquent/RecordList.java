package gaarason.database.contract.eloquent;

import gaarason.database.contract.record.FriendlyList;
import gaarason.database.contract.record.RelationshipList;
import gaarason.database.support.Column;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.RandomAccess;
import java.util.Set;

/**
 * 结果集集合
 * @author xt
 */
public interface RecordList<T, K> extends FriendlyList<T, K>, RelationshipList<T, K>, List<Record<T, K>>,
    RandomAccess, Cloneable, Serializable {

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
    Map<String, Set<String>> getCacheMap();

    /**
     * 内存缓存
     * @param cacheMap 内存缓存
     */
    void setCacheMap(Map<String, Set<String>> cacheMap);
}
