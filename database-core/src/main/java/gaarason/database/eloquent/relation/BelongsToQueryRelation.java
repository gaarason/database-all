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
import gaarason.database.util.ObjectUtils;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.*;

/**
 * 反向一对一关系
 * @author xt
 */
public class BelongsToQueryRelation extends BaseRelationSubQuery {

    private final BelongsToTemplate belongsToTemplate;

    /**
     * 关系键的默认值, 仅在解除关系时使用
     */
    private final String defaultLocalModelForeignKeyValue;

    public BelongsToQueryRelation(Field field) {
        belongsToTemplate = new BelongsToTemplate(field);

        defaultLocalModelForeignKeyValue = ModelShadowProvider.getByEntityClass(ObjectUtils.typeCast(field.getDeclaringClass()))
            .getColumnFieldMap().get(belongsToTemplate.localModelForeignKey).getDefaultValue();
    }

    @Override
    public String[] prepareSqlArr(List<Map<String, Column>> stringColumnMapList,
        GenerateSqlPartFunctionalInterface<?, ?> generateSqlPart) {
        return new String[]{generateSqlPart.execute(ObjectUtils.typeCast(belongsToTemplate.parentModel.newQuery()))
            .whereIn(belongsToTemplate.parentModelLocalKey,
                getColumnInMapList(stringColumnMapList, belongsToTemplate.localModelForeignKey))
            .toSql(SqlType.SELECT), ""};
    }

    @Override
    public RecordList<?, ?> dealBatch(String sql0, RecordList<?, ?> relationRecordList) {
        return belongsToTemplate.parentModel.newQuery().queryList(sql0, new ArrayList<>());
    }

    @Override
    public List<? extends Serializable> filterBatchRecord(Record<?, ?> theRecord, RecordList<?, ?> targetRecordList,
        Map<String, RecordList<?, ?>> cacheRelationRecordList) {
        // 父表的外键字段名
        String column = belongsToTemplate.parentModelLocalKey;
        // 本表的关系键值
        Object value = theRecord.getMetadataMap().get(belongsToTemplate.localModelForeignKey).getValue();

        return findObjList(targetRecordList.toObjectList(cacheRelationRecordList), column, value);
    }

    @Override
    public int attach(Record<?, ?> theRecord, RecordList<?, ?> targetRecords, Map<String, String> stringStringMap) {
        if (targetRecords.isEmpty()) {
            return 0;
        }

        // 目标表(父表)model的关联键值
        Object parentModelLocalKeyValue = parentModelLocalKeyValue(targetRecords);

        // 执行更新, 自我更新需要手动刷新属性
        return attachAndRefresh(theRecord, parentModelLocalKeyValue);
    }

    @Override
    public int attach(Record<?, ?> theRecord, Collection<Object> targetPrimaryKeyValues, Map<String, String> stringStringMap) {
        if (targetPrimaryKeyValues.isEmpty()) {
            return 0;
        } else if (targetPrimaryKeyValues.size() > 1) {
            throw new RelationAttachException("The relationship \"@BelongsTo\" could only attach a relationship with " +
                "one, but now more than one.");
        }

        // 目标表(父表)model的主键
        Object targetPrimaryKeyValue = targetPrimaryKeyValues.toArray()[0];

        return theRecord.getModel().newQuery().transaction(() -> {
            // 目标表(父表)model的关联键值
            Object parentModelLocalKeyValue = belongsToTemplate.parentModel.newQuery()
                .select(belongsToTemplate.parentModelLocalKey)
                .where(belongsToTemplate.parentModel.getPrimaryKeyColumnName(), targetPrimaryKeyValue)
                .firstOrFail().getMetadataMap().get(belongsToTemplate.parentModelLocalKey).getValue();

            // 执行更新, 自我更新需要手动刷新属性
            return attachAndRefresh(theRecord, parentModelLocalKeyValue);
        });
    }

    @Override
    public int detach(Record<?, ?> theRecord) {
        return detach(theRecord, Collections.singletonList(
            theRecord.getMetadataMap().get(belongsToTemplate.localModelForeignKey).getValue().toString()));
    }

    @Override
    public int detach(Record<?, ?> theRecord, RecordList<?, ?> targetRecords) {
        // 应该更新的子表的主键列表
        List<Object> targetRecordPrimaryKeyIds = targetRecords.toList(
            recordTemp -> recordTemp.getMetadataMap().get(recordTemp.getModel().getPrimaryKeyColumnName()).getValue());
        return detach(theRecord, targetRecordPrimaryKeyIds);
    }

    @Override
    public int detach(Record<?, ?> theRecord, Collection<Object> targetPrimaryKeyValues) {
        if (targetPrimaryKeyValues.isEmpty()) {
            return 0;
        }
        // 执行更新, 自我更新需要手动刷新属性
        // 目标,必须是关联关系, 才解除
        // 解除可以多个
        int successNum = theRecord.getModel().newQuery()
            .where(theRecord.getModel().getPrimaryKeyColumnName(), String.valueOf(theRecord.getOriginalPrimaryKeyValue()))
            .whereIn(belongsToTemplate.localModelForeignKey,
                (builder -> builder.select(belongsToTemplate.parentModelLocalKey)
                    .from(belongsToTemplate.parentModel.getTableName())
                    .whereIn(belongsToTemplate.parentModel.getPrimaryKeyColumnName(), targetPrimaryKeyValues)))
            .data(belongsToTemplate.localModelForeignKey, defaultLocalModelForeignKeyValue).update();
        if (successNum > 0) {
            Map<String, Column> metadataMap = theRecord.getMetadataMap();
            metadataMap.get(belongsToTemplate.localModelForeignKey).setValue(defaultLocalModelForeignKeyValue);
            theRecord.refresh(metadataMap);
        }
        return successNum;
    }

    @Override
    public int sync(Record<?, ?> theRecord, RecordList<?, ?> targetRecords, Map<String, String> stringStringMap) {
        return attach(theRecord, targetRecords, stringStringMap);
    }

    @Override
    public int sync(Record<?, ?> theRecord, Collection<Object> targetPrimaryKeyValues, Map<String, String> stringStringMap) {
        return attach(theRecord, targetPrimaryKeyValues, stringStringMap);
    }

    @Override
    public int toggle(Record<?, ?> theRecord, RecordList<?, ?> targetRecords, Map<String, String> stringStringMap) {
        if (targetRecords.isEmpty()){
            return 0;
        }

        // 目标表(父表)model的关联键值
        Object parentModelLocalKeyValue = parentModelLocalKeyValue(targetRecords);

        // 关系已经存在, 切换即是解除
        if (theRecord.getMetadataMap().get(belongsToTemplate.localModelForeignKey).getValue().equals(parentModelLocalKeyValue)) {
            return detach(theRecord, Collections.singletonList(parentModelLocalKeyValue));
        }
        // 关系不存在, 切换即是增加
        else {
            return attachAndRefresh(theRecord, parentModelLocalKeyValue);
        }

    }

    @Override
    public int toggle(Record<?, ?> theRecord, Collection<Object> targetPrimaryKeyValues, Map<String, String> stringStringMap) {
        if (targetPrimaryKeyValues.isEmpty()) {
            return 0;
        } else if (targetPrimaryKeyValues.size() > 1) {
            throw new RelationAttachException("The relationship \"@BelongsTo\" could only toggle a relationship with " +
                "one, but now more than one.");
        }

        // 目标表(父表)model的主键
        Object targetPrimaryKeyValue = targetPrimaryKeyValues.toArray()[0];

        return belongsToTemplate.parentModel.newQuery().transaction(() -> {

            // 目标表(父表)model的关联键值
            Object parentModelLocalKeyValue = belongsToTemplate.parentModel.newQuery()
                .select(belongsToTemplate.parentModelLocalKey)
                .where(belongsToTemplate.parentModel.getPrimaryKeyColumnName(), targetPrimaryKeyValue)
                .firstOrFail().getMetadataMap().get(belongsToTemplate.parentModelLocalKey).getValue();

            // 关系已经存在, 切换即是解除
            if (theRecord.getMetadataMap().get(belongsToTemplate.localModelForeignKey).getValue().equals(parentModelLocalKeyValue)) {
                return detach(theRecord, targetPrimaryKeyValues);
            }
            // 关系不存在, 切换即是增加
            else {
                return attachAndRefresh(theRecord, parentModelLocalKeyValue);
            }
        });
    }

    protected Object parentModelLocalKeyValue(RecordList<?, ?> targetRecords) {
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
        return value;
    }

    /**
     * 增加关系并刷新自身
     * @param theRecord                 本模型
     * @param localModelForeignKeyValue 父模型的关系键(本模型外键)
     * @return 受影响的行数
     */
    protected int attachAndRefresh(Record<?, ?> theRecord, Object localModelForeignKeyValue) {
        // 执行更新, 自我更新需要手动刷新属性
        int successNum = theRecord.getModel().newQuery()
            .where(theRecord.getModel().getPrimaryKeyColumnName(), String.valueOf(theRecord.getOriginalPrimaryKeyValue()))
            .data(belongsToTemplate.localModelForeignKey, localModelForeignKeyValue).update();
        if (successNum > 0) {
            Map<String, Column> metadataMap = theRecord.getMetadataMap();
            metadataMap.get(belongsToTemplate.localModelForeignKey).setValue(localModelForeignKeyValue);
            theRecord.refresh(metadataMap);
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