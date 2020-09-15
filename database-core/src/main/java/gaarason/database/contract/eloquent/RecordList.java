package gaarason.database.contract.eloquent;

import gaarason.database.contract.record.FriendlyList;
import gaarason.database.contract.record.RelationshipList;
import gaarason.database.support.Column;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.RandomAccess;
import java.util.Set;

public interface RecordList<T, K> extends FriendlyList<T, K>, RelationshipList<T, K>, List<Record<T, K>>,
        RandomAccess, Cloneable, Serializable {

    /**
     * 元数据
     * @return 元数据
     */
    List<Map<String, Column>> getOriginalMetadataMapList();

    /**
     * 元数据
     * @param originalMetadataMapList 元数据
     */
    void setOriginalMetadataMapList(List<Map<String, Column>> originalMetadataMapList);

    /**
     * 原始sql
     * @return sql
     */
    String getOriginalSql();

    /**
     * 原始sql
     * @param originalSql 原始sql
     */
    void setOriginalSql(String originalSql);

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
