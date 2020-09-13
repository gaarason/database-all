package gaarason.database.contract.eloquent.relation;

import gaarason.database.contract.function.GenerateSqlPart;
import gaarason.database.eloquent.Record;
import gaarason.database.eloquent.RecordList;
import gaarason.database.support.Column;

import java.util.List;
import java.util.Map;

public interface SubQuery {

    /**
     * 批量关联查询的sql预生成, 可以作为缓存key
     * @param stringColumnMapList 当前recordList的元数据
     * @param generateSqlPart     Builder
     * @return sql数组
     */
    String[] dealBatchSql(List<Map<String, Column>> stringColumnMapList, GenerateSqlPart generateSqlPart);

    /**
     * 批量关联查询
     * @param sqlArr sql数组
     * @return 查询结果集
     */
    RecordList<?, ?> dealBatch(String[] sqlArr);

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
