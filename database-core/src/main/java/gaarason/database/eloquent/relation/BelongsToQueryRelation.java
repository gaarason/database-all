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
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class BelongsToQueryRelation extends BaseRelationSubQuery {

    private final BelongsToTemplate belongsToTemplate;

    public BelongsToQueryRelation(Field field) {
        belongsToTemplate = new BelongsToTemplate(field);
    }

    @Override
    public String[] prepareSqlArr(List<Map<String, Column>> stringColumnMapList,
                                  GenerateSqlPartFunctionalInterface generateSqlPart) {
        return new String[]{generateSqlPart.execute(belongsToTemplate.parentModel.newQuery())
            .whereIn(belongsToTemplate.parentModelLocalKey,
                getColumnInMapList(stringColumnMapList, belongsToTemplate.localModelForeignKey))
            .toSql(SqlType.SELECT), ""};
    }

    @Override
    public RecordList<?, ?> dealBatch(String sql0, RecordList<?, ?> relationRecordList) {
        return belongsToTemplate.parentModel.newQuery().queryList(sql0, new ArrayList<>());
    }

    @Override
    public List<?> filterBatchRecord(Record<?, ?> record, RecordList<?, ?> TargetRecordList,
                                     Map<String, RecordList<?, ?>> cacheRelationRecordList) {
        // 父表的外键字段名
        String column = belongsToTemplate.parentModelLocalKey;
        // 本表的关系键值
        String value = String.valueOf(
            record.getMetadataMap().get(belongsToTemplate.localModelForeignKey).getValue());

        return findObjList(TargetRecordList.toObjectList(cacheRelationRecordList), column, value);
    }

    @Override
    public void attach(Record<?, ?> record, RecordList<?, ?> targetRecords, Map<String, String> stringStringMap) {
        // 应该更新的子表的主键列表
        List<String> targetRecordPrimaryKeyIds = targetRecords.toList(
            recordTemp -> String.valueOf(
                recordTemp.getMetadataMap().get(belongsToTemplate.parentModelLocalKey).getValue()));
        attach(record, targetRecordPrimaryKeyIds, stringStringMap);
    }

    @Override
    public void attach(Record<?, ?> record, Collection<String> targetPrimaryKeyValues,
                       Map<String, String> stringStringMap) {
        if (targetPrimaryKeyValues.size() == 0)
            return;
        else if (targetPrimaryKeyValues.size() > 1) {
            throw new RelationAttachException("The relationship \"@BelongsTo\" could only attach a relationship with " +
                "one, but now more than one.");
        }

        // 目标表(父表)model的关联键值
        String parentModelLocalKeyValue = String.valueOf(targetPrimaryKeyValues.toArray()[0]);


        // 执行更新, 自我更新需要手动刷新属性
        boolean success = record.getModel().newQuery()
            .where(record.getModel().getPrimaryKeyColumnName(), String.valueOf(record.getOriginalPrimaryKeyValue()))
            .data(belongsToTemplate.localModelForeignKey, parentModelLocalKeyValue).update() > 0;
        if(success){
            Map<String, Column> metadataMap = record.getMetadataMap();
            metadataMap.get(belongsToTemplate.localModelForeignKey).setValue(parentModelLocalKeyValue);
            record.refresh(metadataMap);
        }
    }

    static class BelongsToTemplate {
        final Model<?, ?> parentModel;

        final String localModelForeignKey;

        final String parentModelLocalKey;

        BelongsToTemplate(Field field) {
            BelongsTo belongsTo = field.getAnnotation(BelongsTo.class);
            parentModel = getModelInstance(field);
            localModelForeignKey = belongsTo.localModelForeignKey();
            parentModelLocalKey = "".equals(belongsTo.parentModelLocalKey())
                ? parentModel.getPrimaryKeyColumnName()
                : belongsTo.parentModelLocalKey();
        }
    }
}