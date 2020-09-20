package gaarason.database.eloquent.relation;

import gaarason.database.contract.eloquent.Model;
import gaarason.database.contract.eloquent.Record;
import gaarason.database.contract.eloquent.RecordList;
import gaarason.database.contract.function.GenerateSqlPartFunctionalInterface;
import gaarason.database.eloquent.annotation.BelongsTo;
import gaarason.database.eloquent.appointment.SqlType;
import gaarason.database.exception.RelationAttachException;
import gaarason.database.provider.ModelShadowProvider;
import gaarason.database.support.Column;

import java.lang.reflect.Field;
import java.util.*;

public class BelongsToQueryRelation extends BaseRelationSubQuery {

    private final BelongsToTemplate belongsToTemplate;

    /**
     * 关系键的默认值, 仅在解除关系时使用
     */
    private final String defaultLocalModelForeignKeyValue;

    public BelongsToQueryRelation(Field field) {
        belongsToTemplate = new BelongsToTemplate(field);

        defaultLocalModelForeignKeyValue = ModelShadowProvider.getByEntity(field.getDeclaringClass())
                .getColumnFieldMap().get(belongsToTemplate.localModelForeignKey).getDefaultValue();
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
    public int attach(Record<?, ?> record, RecordList<?, ?> targetRecords, Map<String, String> stringStringMap) {
        if (targetRecords.size() == 0)
            return 0;

        // 目标表(父表)model的关联键值
        String parentModelLocalKeyValue = parentModelLocalKeyValue(targetRecords);

        // 执行更新, 自我更新需要手动刷新属性
        return attachAndRefresh(record, parentModelLocalKeyValue);
    }

    @Override
    public int attach(Record<?, ?> record, Collection<String> targetPrimaryKeyValues, Map<String, String> stringStringMap) {
        if (targetPrimaryKeyValues.size() == 0)
            return 0;
        else if (targetPrimaryKeyValues.size() > 1) {
            throw new RelationAttachException("The relationship \"@BelongsTo\" could only attach a relationship with " +
                    "one, but now more than one.");
        }

        // 目标表(父表)model的主键
        String targetPrimaryKeyValue = String.valueOf(targetPrimaryKeyValues.toArray()[0]);

        return record.getModel().newQuery().transaction(() -> {
            // 目标表(父表)model的关联键值
            String parentModelLocalKeyValue = belongsToTemplate.parentModel.newQuery()
                    .select(belongsToTemplate.parentModelLocalKey)
                    .where(belongsToTemplate.parentModel.getPrimaryKeyColumnName(), targetPrimaryKeyValue)
                    .firstOrFail().getMetadataMap().get(belongsToTemplate.parentModelLocalKey).getValue().toString();

            // 执行更新, 自我更新需要手动刷新属性
            return attachAndRefresh(record, parentModelLocalKeyValue);
        });
    }

    @Override
    public int detach(Record<?, ?> record) {
        return detach(record, Collections.singletonList(
                record.getMetadataMap().get(belongsToTemplate.localModelForeignKey).getValue().toString()));
    }

    @Override
    public int detach(Record<?, ?> record, RecordList<?, ?> targetRecords) {
        // 应该更新的子表的主键列表
        List<String> targetRecordPrimaryKeyIds = targetRecords.toList(
                recordTemp -> String.valueOf(
                        recordTemp.getMetadataMap().get(recordTemp.getModel().getPrimaryKeyColumnName()).getValue()));
        return detach(record, targetRecordPrimaryKeyIds);
    }

    @Override
    public int detach(Record<?, ?> record, Collection<String> targetPrimaryKeyValues) {
        if (targetPrimaryKeyValues.size() == 0) {
            return 0;
        }
        // 执行更新, 自我更新需要手动刷新属性
        // 目标,必须是关联关系, 才解除
        // 解除可以多个
        int successNum = record.getModel().newQuery()
                .where(record.getModel().getPrimaryKeyColumnName(), String.valueOf(record.getOriginalPrimaryKeyValue()))
                .whereIn(belongsToTemplate.localModelForeignKey,
                        (builder -> builder.select(belongsToTemplate.parentModelLocalKey)
                                .from(belongsToTemplate.parentModel.getTableName())
                                .whereIn(belongsToTemplate.parentModel.getPrimaryKeyColumnName(), targetPrimaryKeyValues)))
                .data(belongsToTemplate.localModelForeignKey, defaultLocalModelForeignKeyValue).update();
        if (successNum > 0) {
            Map<String, Column> metadataMap = record.getMetadataMap();
            metadataMap.get(belongsToTemplate.localModelForeignKey).setValue(defaultLocalModelForeignKeyValue);
            record.refresh(metadataMap);
        }
        return successNum;
    }

    @Override
    public int sync(Record<?, ?> record, RecordList<?, ?> targetRecords, Map<String, String> stringStringMap) {
        return attach(record, targetRecords, stringStringMap);
    }

    @Override
    public int sync(Record<?, ?> record, Collection<String> targetPrimaryKeyValues, Map<String, String> stringStringMap) {
        return attach(record, targetPrimaryKeyValues, stringStringMap);
    }

    @Override
    public int toggle(Record<?, ?> record, RecordList<?, ?> targetRecords, Map<String, String> stringStringMap) {
        if (targetRecords.size() == 0)
            return 0;

        // 目标表(父表)model的关联键值
        String parentModelLocalKeyValue = parentModelLocalKeyValue(targetRecords);

        // 关系已经存在, 切换即是解除
        if (record.getMetadataMap().get(belongsToTemplate.localModelForeignKey).getValue().toString().equals(parentModelLocalKeyValue)) {
            return detach(record, Collections.singletonList(parentModelLocalKeyValue));
        }
        // 关系已经存在, 切换即是增加
        else{
            return attachAndRefresh(record, parentModelLocalKeyValue);
        }

    }

    @Override
    public int toggle(Record<?, ?> record, Collection<String> targetPrimaryKeyValues, Map<String, String> stringStringMap) {
        if (targetPrimaryKeyValues.size() == 0)
            return 0;
        else if (targetPrimaryKeyValues.size() > 1) {
            throw new RelationAttachException("The relationship \"@BelongsTo\" could only toggle a relationship with " +
                    "one, but now more than one.");
        }

        // 目标表(父表)model的主键
        String targetPrimaryKeyValue = String.valueOf(targetPrimaryKeyValues.toArray()[0]);

        return belongsToTemplate.parentModel.newQuery().transaction(() -> {

            // 目标表(父表)model的关联键值
            String parentModelLocalKeyValue = belongsToTemplate.parentModel.newQuery()
                    .select(belongsToTemplate.parentModelLocalKey)
                    .where(belongsToTemplate.parentModel.getPrimaryKeyColumnName(), targetPrimaryKeyValue)
                    .firstOrFail().getMetadataMap().get(belongsToTemplate.parentModelLocalKey).getValue().toString();

            // 关系已经存在, 切换即是解除
            if (String.valueOf(record.getMetadataMap().get(belongsToTemplate.localModelForeignKey).getValue()).equals(parentModelLocalKeyValue)) {
                return detach(record, targetPrimaryKeyValues);
            }
            // 关系已经存在, 切换即是增加
            else {
                return attachAndRefresh(record, parentModelLocalKeyValue);
            }
        });
    }

    protected String parentModelLocalKeyValue(RecordList<?, ?> targetRecords){
        if (targetRecords.size() > 1) {
            throw new RelationAttachException("The relationship \"@BelongsTo\" could only attach/toggle/sync a relationship with " +
                    "one, but now more than one.");
        }
        Column column = targetRecords.get(0).getMetadataMap().get(belongsToTemplate.parentModelLocalKey);
        if (null == column) {
            throw new RelationAttachException("Not found the relation key["
                    + belongsToTemplate.parentModelLocalKey + "] in the target records.");
        }
        Object value = column.getValue();
        if (null == value) {
            throw new RelationAttachException("The relation key["
                    + belongsToTemplate.parentModelLocalKey + "] in the target records should not be NULL.");
        }

        // 目标表(父表)model的关联键值
        return String.valueOf(value);
    }

    /**
     * 增加关系并刷新自身
     * @param record                    本模型
     * @param localModelForeignKeyValue 父模型的关系键(本模型外键)
     * @return 受影响的行数
     */
    protected int attachAndRefresh(Record<?, ?> record, String localModelForeignKeyValue) {
        // 执行更新, 自我更新需要手动刷新属性
        int successNum = record.getModel().newQuery()
                .where(record.getModel().getPrimaryKeyColumnName(), String.valueOf(record.getOriginalPrimaryKeyValue()))
                .data(belongsToTemplate.localModelForeignKey, localModelForeignKeyValue).update();
        if (successNum > 0) {
            Map<String, Column> metadataMap = record.getMetadataMap();
            metadataMap.get(belongsToTemplate.localModelForeignKey).setValue(localModelForeignKeyValue);
            record.refresh(metadataMap);
        }
        return successNum;
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