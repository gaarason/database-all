package gaarason.database.eloquent.relations;

import gaarason.database.contracts.function.GenerateSqlPart;
import gaarason.database.contracts.function.RelationshipRecordWith;
import gaarason.database.core.lang.Nullable;
import gaarason.database.eloquent.Model;
import gaarason.database.eloquent.Record;
import gaarason.database.eloquent.RecordList;
import gaarason.database.eloquent.annotations.BelongsTo;
import gaarason.database.query.Builder;
import gaarason.database.support.Column;
import gaarason.database.support.RecordFactory;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

public class BelongsToQuery extends SubQuery {

    static class BelongsToTemplate {
        Model<? ,?> parentModel;

        String foreignKey;

        String localKey;

        BelongsToTemplate(Field field) {
            BelongsTo belongsTo = field.getAnnotation(
                BelongsTo.class);
            parentModel = getModelInstance(belongsTo.parentModel());
            foreignKey = belongsTo.foreignKey();
            localKey = belongsTo.localKey();
            localKey = "".equals(localKey) ? parentModel.getPrimaryKeyColumnName() : localKey;
        }
    }

    /**
     * 单个关联查询, 以及对象转化
     * @param field                  字段
     * @param stringColumnMap        当前record的元数据
     * @param generateSqlPart        Builder
     * @param relationshipRecordWith Record
     * @return 目标实体对象
     */
    @Nullable
    public static Object dealSingle(Field field, Map<String, Column> stringColumnMap,
                                      GenerateSqlPart generateSqlPart,
                                      RelationshipRecordWith relationshipRecordWith) {
//        BelongsTo belongsTo = field.getAnnotation(
//            BelongsTo.class);
//        Model<?, ?> parentModel = getModelInstance(belongsTo.parentModel());
//        String      foreignKey  = belongsTo.foreignKey();
//        String      localKey    = belongsTo.localKey();
//        localKey = "".equals(localKey) ? parentModel.getPrimaryKeyColumnName() : localKey;

        BelongsToTemplate belongsTo = new BelongsToTemplate(field);

        Builder<?, ?> builder = belongsTo.parentModel.newQuery();

        Record<?, ?> record = generateSqlPart.generate(belongsTo.parentModel.newQuery())
            .where(belongsTo.localKey, String.valueOf(stringColumnMap.get(belongsTo.foreignKey).getValue()))
            .first();
        return record == null ? null : relationshipRecordWith.generate(record).toObject();
    }

    /**
     * 批量关联查询
     * @param field                  字段
     * @param stringColumnMapList    当前recordList的元数据
     * @param generateSqlPart        Builder
     * @param relationshipRecordWith Record
     * @return 查询结果集
     */
    public static RecordList<?, ?> dealBatch(Field field, List<Map<String, Column>> stringColumnMapList,
                                                    GenerateSqlPart generateSqlPart,
                                                    RelationshipRecordWith relationshipRecordWith) {
        BelongsToTemplate belongsTo = new BelongsToTemplate(field);

        RecordList<?, ?> records = generateSqlPart.generate(belongsTo.parentModel.newQuery())
            .whereIn(belongsTo.localKey, getColumnInMapList(stringColumnMapList, belongsTo.foreignKey))
            .get();
        for (Record<?, ?> record : records) {
            relationshipRecordWith.generate(record);
        }
        return records;
    }

    /**
     * 筛选批量关联查询结果
     * @param field                  字段
     * @param record                 当前record
     * @param relationshipRecordList 关联的recordList
     * @return 筛选后的查询结果集
     */
    @Nullable
    public static Object filterBatch(Field field, Record<?, ?> record,
                                            RecordList<?, ?> relationshipRecordList) {
        BelongsToTemplate belongsTo = new BelongsToTemplate(field);


        Record<?, ?> newRecord = RecordFactory.filterRecord(relationshipRecordList, belongsTo.localKey,
            String.valueOf(record.getMetadataMap().get(belongsTo.foreignKey).getValue()));

        return newRecord == null ? null : newRecord.toObject();

    }
}
