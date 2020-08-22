package gaarason.database.contracts.eloquent.relations;

import gaarason.database.contracts.function.GenerateSqlPart;
import gaarason.database.eloquent.Record;
import gaarason.database.eloquent.RecordList;
import gaarason.database.support.Column;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface SubQuery {

    /**
     * 将元数据中的关系键值,转化为可以使用 where in 查询的 set
     * @param stringColumnMapList 当前recordList的元数据
     * @return 关系set
     */
    Set<Object> getSetInMapList(List<Map<String, Column>> stringColumnMapList);

    /**
     * 批量关联查询
     * @param setInMapList    关系set
     * @param generateSqlPart Builder
     * @return 查询结果集
     */
    RecordList<?, ?> dealBatch(Set<Object> setInMapList, GenerateSqlPart generateSqlPart);

    /**
     * 筛选批量关联查询结果对象
     * @param record                  当前record
     * @param relationshipRecordList  关联的recordList
     * @param cacheRelationRecordList 结果缓存
     * @return 筛选后的查询结果集
     */
    List<?> filterBatchRecord(Record<?, ?> record, RecordList<?, ?> relationshipRecordList,
                              Map<String, RecordList<?, ?>> cacheRelationRecordList);

}
