package gaarason.database.contracts.function;

import gaarason.database.eloquent.Record;

@FunctionalInterface
public interface RelationshipRecordWith {

    /**
     * Record关联关系
     * @param record Record 查询结果集
     * @return Record 查询结果集
     */
     Record<?, ?> generate(Record<?, ?> record);
}
