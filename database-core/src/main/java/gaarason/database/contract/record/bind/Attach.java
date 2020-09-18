package gaarason.database.contract.record.bind;

import gaarason.database.contract.eloquent.Record;
import gaarason.database.contract.eloquent.RecordList;

import java.util.Collection;
import java.util.Map;

public interface Attach {

    /**
     * 新增关系, 连接模型的中间表中插入记录
     * @param targetRecord 目标关系
     */
    void attach(Record<?, ?> targetRecord);

    void attach(RecordList<?, ?> targetRecords);

    void attach(Record<?, ?> targetRecord, Map<String, String> stringStringMap);

    void attach(RecordList<?, ?> targetRecords, Map<String, String> stringStringMap);

    void attach(String id);

    void attach(Collection<String> ids);

    void attach(String id, Map<String, String> stringStringMap);

    void attach(Collection<String> ids, Map<String, String> stringStringMap);

}
