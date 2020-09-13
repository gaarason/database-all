package gaarason.database.contract.function;

import gaarason.database.eloquent.RecordList;

@FunctionalInterface
public interface GenerateRecordList {

    /**
     * RecordList关联关系
     * @return Record 查询结果集
     */
    RecordList<?, ?> generate();
}
