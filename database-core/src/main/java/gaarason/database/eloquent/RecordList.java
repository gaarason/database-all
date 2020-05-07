package gaarason.database.eloquent;

import gaarason.database.contracts.function.FilterRecordAttribute;

import java.util.*;

public class RecordList<T, K> extends ArrayList<Record<T, K>> {

    /**
     * 转化为对象列表
     * @return 对象列表
     */
    public List<T> toObjectList() {
        List<T> list = new ArrayList<>();
        for (Record<T, K> record : this) {
            list.add(record.toObject());
        }
        return list;
    }

    /**
     * 转化为对象列表(不查询关联关系)
     * @return 对象列表
     */
    public List<T> toObjectWithoutRelationship() {
        List<T> list = new ArrayList<>();
        for (Record<T, K> record : this) {
            list.add(record.toObjectWithoutRelationship());
        }
        return list;
    }

    /**
     * 转化为map list
     * @return mapList
     */
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
}
