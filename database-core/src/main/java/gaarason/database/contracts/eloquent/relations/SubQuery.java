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
    
    RecordList<?, ?> dealBatch(Field field, List<Map<String, Column>> stringColumnMapList,
                               GenerateSqlPart generateSqlPart,
                               RelationshipRecordWith relationshipRecordWith);

    @Nullable
    Object filterBatch(Field field, Record<?, ?> record,
                       RecordList<?, ?> relationshipRecordList, Map<String, RecordList<?, ?>> cacheRelationRecordList);


}
