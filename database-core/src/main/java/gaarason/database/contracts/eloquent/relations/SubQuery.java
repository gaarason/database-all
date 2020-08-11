package gaarason.database.contracts.eloquent.relations;

import gaarason.database.contracts.function.GenerateSqlPart;
import gaarason.database.contracts.function.RelationshipRecordWith;
import gaarason.database.core.lang.Nullable;
import gaarason.database.eloquent.Record;
import gaarason.database.eloquent.RecordList;
import gaarason.database.support.Column;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

public interface SubQuery {
    /**
     * 批量关联查询
     * @param stringColumnMapList    当前recordList的元数据
     * @param generateSqlPart        Builder
     * @param relationshipRecordWith Record
     * @return 查询结果集
     */
    RecordList<?, ?> dealBatch(List<Map<String, Column>> stringColumnMapList,
                               GenerateSqlPart generateSqlPart,
                               RelationshipRecordWith relationshipRecordWith);


    /**
     * 筛选批量关联查询结果
     * @param record                 当前record
     * @param relationshipRecordList 关联的recordList
     * @return 筛选后的查询结果集
     */
    @Nullable
    Object filterBatch(Record<?, ?> record,
                       RecordList<?, ?> relationshipRecordList, Map<String, RecordList<?, ?>> cacheRelationRecordList);


}
