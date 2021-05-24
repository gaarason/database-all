package gaarason.database.eloquent;

import gaarason.database.contract.eloquent.Record;
import gaarason.database.contract.eloquent.RecordList;
import gaarason.database.contract.function.FilterRecordAttributeFunctionalInterface;
import gaarason.database.contract.function.GenerateSqlPartFunctionalInterface;
import gaarason.database.contract.function.RelationshipRecordWithFunctionalInterface;
import gaarason.database.support.Column;
import gaarason.database.support.RelationGetSupport;
import gaarason.database.util.EntityUtil;
import gaarason.database.util.StringUtil;

import java.util.*;

public class RecordListBean<T, K> extends ArrayList<Record<T, K>> implements RecordList<T, K> {

    /**
     * 元数据
     */
    protected final List<Map<String, Column>> originalMetadataMapList = new ArrayList<>();

    /**
     * 原始sql
     */
    protected String originalSql = "";

    /**
     * 临时缓存
     */
    protected Map<String, Set<String>> cacheMap = new HashMap<>();

    public RecordListBean() {

    }

    public RecordListBean(String originalSql) {
        this.originalSql = originalSql;
    }


    @Override
    public List<Map<String, Column>> getOriginalMetadataMapList() {
        return originalMetadataMapList;
    }

    @Override
    public String getOriginalSql() {
        return originalSql;
    }

    @Override
    public Map<String, Set<String>> getCacheMap() {
        return cacheMap;
    }

    @Override
    public void setCacheMap(Map<String, Set<String>> cacheMap) {
        this.cacheMap = cacheMap;
    }

    /**
     * 转化为对象列表
     * @return 对象列表
     */
    @Override
    public List<T> toObjectList() {
        RelationGetSupport<T, K> tkRelationGetSupport = new RelationGetSupport<>(this, true);
        return tkRelationGetSupport.toObjectList();
    }

    @Override
    public List<T> toObjectListWithoutRelationship() {
        RelationGetSupport<T, K> tkRelationGetSupport = new RelationGetSupport<>(this, false);
        return tkRelationGetSupport.toObjectList();
    }

    /**
     * 转化为对象列表
     * @return 对象列表
     */
    @Override
    public List<T> toObjectList(Map<String, RecordList<?, ?>> cacheRelationRecordList) {
        RelationGetSupport<T, K> tkRelationGetSupport = new RelationGetSupport<>(this, true);
        return tkRelationGetSupport.toObjectList(cacheRelationRecordList);
    }

    @Override
    public <V> List<V> toObjectList(Class<V> clazz) {
        return EntityUtil.entityAssignment(this.originalMetadataMapList, clazz);
    }


    /**
     * 转化为map list
     * @return mapList
     */
    @Override
    public List<Map<String, Object>> toMapList() {
        List<Map<String, Object>> list = new ArrayList<>();
        for (Record<T, K> record : this) {
            list.add(record.toMap());
        }
        return list;
    }

    @Override
    public Map<String, List<Object>> toListMap() {
        Map<String, List<Object>> map = new HashMap<>();
        for (Record<T, K> record : this) {
            for (String column : record.getMetadataMap().keySet()) {
                List<Object> list = map.computeIfAbsent(column, (key) -> new ArrayList<>());
                list.add(record.getMetadataMap().get(column).getValue());
            }
        }
        return map;
    }

    /**
     * 过滤成list
     * @return 单个字段列表
     */
    @Override
    public <V> List<V> toList(FilterRecordAttributeFunctionalInterface<T, K, V> filterRecordAttributeFunctionalInterface) {
        List<V> list = new ArrayList<>();
        for (Record<T, K> record : this) {
            V result = filterRecordAttributeFunctionalInterface.execute(record);
            if (null == result)
                continue;
            list.add(result);
        }
        return list;
    }

    @Override
    public List<String> toOneColumnList() {
        return toList(record -> {
            Set<Map.Entry<String, Column>> entries = record.getMetadataMap().entrySet();
            for (Map.Entry<String, Column> entry : entries) {
                Object value = entry.getValue().getValue();
                return value == null ? null : String.valueOf(value);
            }
            return null;
        });
    }

    @Override
    public RecordListBean<T, K> with(String column) {
        return with(column, builder -> builder);
    }

    @Override
    public RecordListBean<T, K> with(String column, GenerateSqlPartFunctionalInterface builderClosure) {
        return with(column, builderClosure, record -> record);
    }

    @Override
    public RecordListBean<T, K> with(String column, GenerateSqlPartFunctionalInterface builderClosure,
                                     RelationshipRecordWithFunctionalInterface recordClosure) {
        String[] columnArr = column.split("\\.");
        // 快捷类型
        if (columnArr.length > 1) {
            String lastLevelColumn = columnArr[columnArr.length - 1];
            String otherLevelColumn = StringUtil.rtrim(column, "." + lastLevelColumn);
            return with(otherLevelColumn, builder -> builder,
                record -> record.with(lastLevelColumn, builderClosure, recordClosure));
        }
        for (Record<T, K> tkRecord : this) {
            // 赋值关联关系过滤
            // 保持引用
            tkRecord.getRelationBuilderMap().put(column, builderClosure);
            tkRecord.getRelationRecordMap().put(column, recordClosure);
        }
        return this;
    }
}
