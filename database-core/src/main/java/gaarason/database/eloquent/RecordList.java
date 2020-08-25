package gaarason.database.eloquent;

import gaarason.database.contracts.function.FilterRecordAttribute;
import gaarason.database.contracts.function.GenerateSqlPart;
import gaarason.database.contracts.function.RelationshipRecordWith;
import gaarason.database.contracts.record.FriendlyListORM;
import gaarason.database.contracts.record.RelationshipListORM;
import gaarason.database.conversion.ToObject;
import gaarason.database.support.Column;
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
        ToObject<T, K> tkToObject = new ToObject<>(this, true);
        return tkToObject.toObjectList();
    }

    @Override
    public List<T> toObjectListWithoutRelationship() {
        ToObject<T, K> tkToObject = new ToObject<>(this, false);
        return tkToObject.toObjectList();
    }

    /**
     * 转化为对象列表
     * @return 对象列表
     */
    public List<T> toObjectList(Map<String, RecordList<?, ?>> cacheRelationRecordList) {
        ToObject<T, K> tkToObject = new ToObject<>(this, true);
        return tkToObject.toObjectList(cacheRelationRecordList);
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


//        for (Record<T, K> tkRecord : this) {
//            // 赋值关联关系过滤
//            tkRecord.with(column, builderClosure, recordClosure);
//        }
//        return this;
    }
}
