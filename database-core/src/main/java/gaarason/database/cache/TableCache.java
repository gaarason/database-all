package gaarason.database.cache;

import gaarason.database.core.lang.Nullable;

import java.util.*;

public class TableCache<R> {

    /**
     * Map<TableName, Map<PrimaryKey, R>>
     */
    private Map<String, Map<String, R>> tableCache = new HashMap<>();

    public synchronized void putOne(String tableName, String id, R rowInfo) {
        Map<String, R> tableCaches;
        // 是否已存在目标表的缓存
        if (tableCache.containsKey(tableName)) {
            // 获取目标表缓存
            tableCaches = tableCache.get(tableName);
        } else {
            // 新增目标表缓存
            tableCaches = new HashMap<>();
            tableCache.put(tableName, tableCaches);
        }
        // 新增目标表的行缓存
        tableCaches.put(id, rowInfo);
    }

    @Nullable
    public synchronized R getOne(String tableName, String id) {
        Map<String, R> tableCaches = tableCache.get(tableName);
        if (tableCaches == null) {
            return null;
        }
        return tableCaches.get(id);
    }

    @Nullable
    public synchronized List<R> getMany(String tableName, Set<String> ids) {
        Map<String, R> tableCaches = tableCache.get(tableName);
        if (tableCaches == null) {
            return null;
        }
        List<R> resultList = new ArrayList<>();
        for (String id : ids) {
            R r = tableCaches.get(id);
            if (null != r) {
                resultList.add(r);
            }
        }
        return resultList;
    }
}
