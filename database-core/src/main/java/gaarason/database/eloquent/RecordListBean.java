package gaarason.database.eloquent;

import gaarason.database.contract.eloquent.Record;
import gaarason.database.contract.eloquent.RecordList;
import gaarason.database.contract.function.FilterRecordAttributeFunctionalInterface;
import gaarason.database.contract.function.GenerateSqlPartFunctionalInterface;
import gaarason.database.contract.function.RelationshipRecordWithFunctionalInterface;
import gaarason.database.core.lang.Nullable;
import gaarason.database.provider.ModelShadowProvider;
import gaarason.database.support.Column;
import gaarason.database.support.RelationGetSupport;
import gaarason.database.util.EntityUtils;
import gaarason.database.util.StringUtils;

import java.io.Serializable;
import java.util.*;

/**
 * 结果集集合对象
 * @param <T>
 * @param <K>
 * @author xt
 */
public class RecordListBean<T extends Serializable, K extends Serializable> extends ArrayList<Record<T, K>> implements RecordList<T, K> {

    /**
     * 元数据
     */
    protected final ArrayList<Map<String, Column>> originalMetadataMapList = new ArrayList<>();

    /**
     * 原始sql
     */
    protected String originalSql = "";

    /**
     * 临时缓存
     */
    protected HashMap<Object, Set<Object>> cacheMap = new HashMap<>();

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
    public HashMap<Object, Set<Object>> getCacheMap() {
        return cacheMap;
    }

    @Override
    public void setCacheMap(HashMap<Object, Set<Object>> cacheMap) {
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

    @Override
    public List<T> toObjectList(Map<String, RecordList<?, ?>> cacheRelationRecordList) {
        RelationGetSupport<T, K> tkRelationGetSupport = new RelationGetSupport<>(this, true);
        return tkRelationGetSupport.toObjectList(cacheRelationRecordList);
    }

    @Override
    public <V> List<V> toObjectList(Class<V> clazz) {
        return EntityUtils.entityAssignment(this.originalMetadataMapList, clazz);
    }


    /**
     * 转化为map list
     * @return mapList
     */
    @Override
    public List<Map<String, Object>> toMapList() {
        List<Map<String, Object>> list = new ArrayList<>();
        for (Record<T, K> theRecord : this) {
            list.add(theRecord.toMap());
        }
        return list;
    }

    @Override
    public Map<String, List<Object>> toListMap() {
        Map<String, List<Object>> map = new HashMap<>(16);
        for (Record<T, K> theRecord : this) {
            for (String column : theRecord.getMetadataMap().keySet()) {
                List<Object> list = map.computeIfAbsent(column, (key) -> new ArrayList<>());
                list.add(theRecord.getMetadataMap().get(column).getValue());
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
        for (Record<T, K> theRecord : this) {
            V result = filterRecordAttributeFunctionalInterface.execute(theRecord);
            if (null == result){
                continue;
            }
            list.add(result);
        }
        return list;
    }

    @Override
    public List<Object> toOneColumnList() {
        return toList(theRecord -> {
            Set<Map.Entry<String, Column>> entries = theRecord.getMetadataMap().entrySet();
            for (Map.Entry<String, Column> entry : entries) {
                return entry.getValue().getValue();
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
        return with(column, builderClosure, theRecord -> theRecord);
    }

    @Override
    public RecordListBean<T, K> with(String column, GenerateSqlPartFunctionalInterface builderClosure,
                                     RelationshipRecordWithFunctionalInterface recordClosure) {
        String[] columnArr = column.split("\\.");
        // 快捷类型
        if (columnArr.length > 1) {
            String lastLevelColumn = columnArr[columnArr.length - 1];
            String otherLevelColumn = StringUtils.rtrim(column, "." + lastLevelColumn);
            return with(otherLevelColumn, builder -> builder,
                theRecord -> theRecord.with(lastLevelColumn, builderClosure, recordClosure));
        }
        for (Record<T, K> tkRecord : this) {
            // 赋值关联关系过滤
            // 保持引用
            tkRecord.getRelationBuilderMap().put(column, builderClosure);
            tkRecord.getRelationRecordMap().put(column, recordClosure);
        }
        return this;
    }

    @Override
    public String toString() {
        return toMapList().toString();
    }

    @Override
    @Nullable
    public Object getValueByFieldName(Record<T, K> theRecord, String fieldName) {
        final ModelShadowProvider.FieldInfo fieldInfo = ModelShadowProvider.getFieldInfoByEntityClass(
            theRecord.getModel().getEntityClass(), fieldName);
        final Column column = theRecord.getMetadataMap().get(fieldInfo.getColumnName());
        return column != null ? column.getValue() : null;
    }
}
