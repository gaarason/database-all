package gaarason.database.eloquent.relation;

import gaarason.database.contract.eloquent.Model;
import gaarason.database.contract.eloquent.Record;
import gaarason.database.contract.eloquent.RecordList;
import gaarason.database.contract.function.GenerateSqlPartFunctionalInterface;
import gaarason.database.eloquent.annotation.BelongsTo;
import gaarason.database.eloquent.appointment.SqlType;
import gaarason.database.exception.RelationAttachException;
import gaarason.database.support.Column;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BelongsToQueryRelation extends BaseRelationSubQuery {

    private final BelongsToTemplate belongsToTemplate;

    public BelongsToQueryRelation(Field field) {
        belongsToTemplate = new BelongsToTemplate(field);
    }

    @Override
    public String[] dealBatchSql(List<Map<String, Column>> stringColumnMapList,
                                 GenerateSqlPartFunctionalInterface generateSqlPart) {
        return new String[]{generateSqlPart.execute(belongsToTemplate.parentModel.newQuery())
            .whereIn(belongsToTemplate.parentModelLocalKey,
                getColumnInMapList(stringColumnMapList, belongsToTemplate.localModelForeignKey))
            .toSql(SqlType.SELECT)};
    }

    @Override
    public RecordList<?, ?> dealBatch(String[] sql) {
        return belongsToTemplate.parentModel.newQuery().queryList(sql[0], new ArrayList<>());
    }

    @Override
    public List<?> filterBatchRecord(Record<?, ?> record, RecordList<?, ?> relationshipRecordList,
                                     Map<String, RecordList<?, ?>> cacheRelationRecordList) {
        // 父表的外键字段名
        String column = belongsToTemplate.parentModelLocalKey;
        // 本表的关系键值
        String value = String.valueOf(
            record.getMetadataMap().get(belongsToTemplate.localModelForeignKey).getValue());

        return findObjList(relationshipRecordList.toObjectList(cacheRelationRecordList), column, value);
    }

    @Override
    public void attach(Record<?, ?> record, RecordList<?, ?> targetRecords, Map<String, String> stringStringMap) {
        if (targetRecords.size() == 0)
            return;
        else if (targetRecords.size() > 1) {
            throw new RelationAttachException("The relationship \"@BelongsTo\" could only attach a relationship with " +
                "one, but now more than one.");
        }

        // 目标表(父表)model的关联键值
        String parentModelLocalKeyValue = String.valueOf(
            targetRecords.get(0).getMetadataMap().get(belongsToTemplate.parentModelLocalKey));

        // 执行更新
        record.getModel().newQuery().data(belongsToTemplate.localModelForeignKey, parentModelLocalKeyValue).update();


    }

    static class BelongsToTemplate {
        Model<?, ?> parentModel;

        String localModelForeignKey;

        String parentModelLocalKey;

        BelongsToTemplate(Field field) {
            BelongsTo belongsTo = field.getAnnotation(BelongsTo.class);
            parentModel = getModelInstance(belongsTo.parentModel());
            localModelForeignKey = belongsTo.localModelForeignKey();
            parentModelLocalKey = belongsTo.parentModelLocalKey();
            parentModelLocalKey = "".equals(
                parentModelLocalKey) ? parentModel.getPrimaryKeyColumnName() : parentModelLocalKey;
        }
    }
}