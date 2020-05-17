package gaarason.database.contracts.function;

import gaarason.database.eloquent.Record;

@FunctionalInterface
public interface RelationshipRecordWith<T, K> {

    /**
     * Record关联关系
     * @param record Record 查询结果集
     * @return Record 查询结果集
     */
     Record<T, K> generate(Record<T, K> record);
}
