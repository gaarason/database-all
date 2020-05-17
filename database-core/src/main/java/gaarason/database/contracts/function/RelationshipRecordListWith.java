package gaarason.database.contracts.function;

import gaarason.database.eloquent.RecordList;

@FunctionalInterface
public interface RelationshipRecordListWith<T, K> {

    /**
     * RecordList关联关系
     * @param recordList RecordList 查询结果集
     * @return Record 查询结果集
     */
    RecordList<T, K> generate(RecordList<T, K> recordList);
}
