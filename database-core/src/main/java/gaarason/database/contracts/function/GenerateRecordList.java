package gaarason.database.contracts.function;

import gaarason.database.eloquent.RecordList;

@FunctionalInterface
public interface GenerateRecordList<T, K> {

    /**
     * RecordList关联关系
     * @return Record 查询结果集
     */
    RecordList<T, K> generate();
}
