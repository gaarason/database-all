package gaarason.database.eloquent.relations;

import gaarason.database.contracts.function.GenerateSqlPart;
import gaarason.database.contracts.function.RelationshipRecordWith;
import gaarason.database.core.lang.Nullable;
import gaarason.database.eloquent.Model;
import gaarason.database.eloquent.Record;
import gaarason.database.eloquent.RecordList;
import gaarason.database.eloquent.annotations.BelongsTo;
import gaarason.database.support.Column;
import gaarason.database.support.RecordFactory;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

public class BelongsToQuery extends BaseSubQuery {

    private final BelongsToTemplate belongsToTemplate;


    static class BelongsToTemplate {
        Model<? ,?> parentModel;

        String localModelForeignKey;

        String parentModelLocalKey;

        BelongsToTemplate(Field field) {
            BelongsTo belongsTo = field.getAnnotation(
                BelongsTo.class);
            parentModel = getModelInstance(belongsTo.parentModel());
            localModelForeignKey = belongsTo.localModelForeignKey();
            parentModelLocalKey = belongsTo.parentModelLocalKey();
            parentModelLocalKey = "".equals(parentModelLocalKey) ? parentModel.getPrimaryKeyColumnName() : parentModelLocalKey;
        }
    }

    public BelongsToQuery(Field field){
        belongsToTemplate = new BelongsToTemplate(field);
    }

    /**
     * 批量关联查询
     * @param stringColumnMapList    当前recordList的元数据
     * @param generateSqlPart        Builder
     * @param relationshipRecordWith Record
     * @return 查询结果集
     */
    @Override
    public  RecordList<?, ?> dealBatch(List<Map<String, Column>> stringColumnMapList,
                                                    GenerateSqlPart generateSqlPart,
                                                    RelationshipRecordWith relationshipRecordWith) {

        RecordList<?, ?> records = generateSqlPart.generate(belongsToTemplate.parentModel.newQuery())
            .whereIn(belongsToTemplate.parentModelLocalKey, getColumnInMapList(stringColumnMapList, belongsToTemplate.localModelForeignKey))
            .get();
//        for (Record<?, ?> record : records) {
//            relationshipRecordWith.generate(record);
//        }
        return records;
    }

    /**
     * 筛选批量关联查询结果
     * @param record                 当前record
     * @param relationshipRecordList 关联的recordList
     * @return 筛选后的查询结果集
     */
    @Nullable
    @Override
    public  Object filterBatch(Record<?, ?> record,
                                            RecordList<?, ?> relationshipRecordList, Map<String, RecordList<?, ?>> cacheRelationRecordList) {

        Record<?, ?> newRecord = RecordFactory.filterRecord(relationshipRecordList, belongsToTemplate.parentModelLocalKey,
            String.valueOf(record.getMetadataMap().get(belongsToTemplate.localModelForeignKey).getValue()));

        return newRecord == null ? null : newRecord.toObject(cacheRelationRecordList);

    }
}
