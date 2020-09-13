package gaarason.database.eloquent;

import gaarason.database.contract.function.FilterRecordAttribute;
import gaarason.database.contract.function.GenerateSqlPart;
import gaarason.database.contract.function.RelationshipRecordWith;
import gaarason.database.contract.record.FriendlyListORM;
import gaarason.database.contract.record.RelationshipListORM;
import gaarason.database.support.RelationGetSupport;
import gaarason.database.support.Column;
import gaarason.database.utils.EntityUtil;
import gaarason.database.utils.StringUtil;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

public class RecordList<T, K> extends ArrayList<Record<T, K>> implements FriendlyListORM<T, K>,
    RelationshipListORM<T, K> {

    /**
     * 元数据
     */
    @Getter
    @Setter
    private List<Map<String, Column>> originalMetadataMapList = new ArrayList<>();

    /**
     * sql
     */
    @Getter
    @Setter
    private String originalSql = "";


    @Getter
    @Setter
    private Map<String, Set<String>> cacheMap = new HashMap<>();


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

    /**
     * 过滤成list
     * @return 单个字段列表
     */
    @Override
    public List<Object> toList(FilterRecordAttribute<T, K> filterRecordAttribute) {
        List<Object> list = new ArrayList<>();
        for (Record<T, K> record : this) {
            Object result = filterRecordAttribute.filter(record);
            if (null == result)
                continue;
            list.add(result);
        }
        return list;
    }


    @Override
    public RecordList<T, K> with(String column) {
        return with(column, builder -> builder);
    }

    @Override
    public RecordList<T, K> with(String column, GenerateSqlPart builderClosure) {
        return with(column, builderClosure, record -> record);
    }

    @Override
    public RecordList<T, K> with(String column, GenerateSqlPart builderClosure,
                                 RelationshipRecordWith recordClosure) {
        String[] columnArr = column.split("\\.");
        // 快捷类型
        if (columnArr.length > 1) {
            String lastLevelColumn  = columnArr[columnArr.length - 1];
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
