package gaarason.database.eloquent.relation;

import gaarason.database.contract.eloquent.Model;
import gaarason.database.contract.eloquent.Record;
import gaarason.database.contract.eloquent.RecordList;
import gaarason.database.contract.function.GenerateSqlPartFunctionalInterface;
import gaarason.database.eloquent.annotation.HasOneOrMany;
import gaarason.database.eloquent.appointment.SqlType;
import gaarason.database.provider.ModelShadowProvider;
import gaarason.database.support.Column;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 一对一/一对多关系
 * @author xt
 */
public class HasOneOrManyQueryRelation extends BaseRelationSubQuery {

    private final HasOneOrManyTemplate hasOneOrManyTemplate;

    /**
     * 目标模型外键的默认值, 仅在解除关系时使用
     */
    private final String defaultSonModelForeignKeyValue;

    public HasOneOrManyQueryRelation(Field field) {
        hasOneOrManyTemplate = new HasOneOrManyTemplate(field);

        defaultSonModelForeignKeyValue = ModelShadowProvider.get(hasOneOrManyTemplate.sonModel)
                .getColumnFieldMap().get(hasOneOrManyTemplate.sonModelForeignKey).getDefaultValue();
    }

    @Override
    public String[] prepareSqlArr(List<Map<String, Column>> stringColumnMapList,
                                  GenerateSqlPartFunctionalInterface generateSqlPart) {
        return new String[]{generateSqlPart.execute(hasOneOrManyTemplate.sonModel.newQuery())
                .whereIn(hasOneOrManyTemplate.sonModelForeignKey,
                        getColumnInMapList(stringColumnMapList, hasOneOrManyTemplate.localModelLocalKey))
                .toSql(SqlType.SELECT), ""};
    }

    @Override
    public RecordList<?, ?> dealBatch(String sql0, RecordList<?, ?> relationRecordList) {
        return hasOneOrManyTemplate.sonModel.newQuery().queryList(sql0, new ArrayList<>());
    }

    @Override
    public List<? extends Serializable> filterBatchRecord(Record<?, ?> theRecord, RecordList<?, ?> targetRecordList,
                                     Map<String, RecordList<?, ?>> cacheRelationRecordList) {
        // 子表的外键字段名
        String column = hasOneOrManyTemplate.sonModelForeignKey;
        // 本表的关系键值
        String value = String.valueOf(theRecord.getMetadataMap().get(hasOneOrManyTemplate.localModelLocalKey).getValue());

        return findObjList(targetRecordList.toObjectList(cacheRelationRecordList), column, value);
    }

    @Override
    public int attach(Record<?, ?> theRecord, RecordList<?, ?> targetRecords, Map<String, String> stringStringMap) {
        return attach(theRecord, getTargetRecordPrimaryKeyIds(targetRecords), stringStringMap);
    }

    @Override
    public int attach(Record<?, ?> theRecord, Collection<String> targetPrimaryKeyValues,
                      Map<String, String> stringStringMap) {
        if (targetPrimaryKeyValues.isEmpty()){
            return 0;
        }

        // 关联键值(当前表关系键(默认当前表主键))(子表外键)
        String relationKeyValue = String.valueOf(theRecord.getMetadataMap().get(hasOneOrManyTemplate.localModelLocalKey).getValue());

        // 执行更新
        return hasOneOrManyTemplate.sonModel.newQuery()
                .whereIn(hasOneOrManyTemplate.sonModel.getPrimaryKeyColumnName(), targetPrimaryKeyValues)
                .where(hasOneOrManyTemplate.sonModelForeignKey, "!=", relationKeyValue)
                .data(hasOneOrManyTemplate.sonModelForeignKey, relationKeyValue).update();
    }

    @Override
    public int detach(Record<?, ?> theRecord) {
        // 关联键值(当前表关系键(默认当前表主键))(子表外键)
        String relationKeyValue = String.valueOf(theRecord.getMetadataMap().get(hasOneOrManyTemplate.localModelLocalKey).getValue());

        // 执行更新
        // 目标,必须是关联关系, 才解除
        return hasOneOrManyTemplate.sonModel.newQuery()
                .where(hasOneOrManyTemplate.sonModelForeignKey, relationKeyValue)
                .data(hasOneOrManyTemplate.sonModelForeignKey, defaultSonModelForeignKeyValue).update();
    }

    @Override
    public int detach(Record<?, ?> theRecord, RecordList<?, ?> targetRecords) {
        // 应该更新的子表的主键列表
        List<String> targetRecordPrimaryKeyIds = targetRecords.toList(
                recordTemp -> String.valueOf(
                        recordTemp.getMetadataMap().get(recordTemp.getModel().getPrimaryKeyColumnName()).getValue()));
        return detach(theRecord, targetRecordPrimaryKeyIds);
    }

    @Override
    public int detach(Record<?, ?> theRecord, Collection<String> targetPrimaryKeyValues) {
        if (targetPrimaryKeyValues.isEmpty()){
            return 0;
        }

        // 关联键值(当前表关系键(默认当前表主键))(子表外键)
        String relationKeyValue = String.valueOf(theRecord.getMetadataMap().get(hasOneOrManyTemplate.localModelLocalKey).getValue());

        // 执行更新
        // 目标,必须是关联关系, 才解除
        return hasOneOrManyTemplate.sonModel.newQuery()
                .whereIn(hasOneOrManyTemplate.sonModel.getPrimaryKeyColumnName(), targetPrimaryKeyValues)
                .where(hasOneOrManyTemplate.sonModelForeignKey, relationKeyValue)
                .data(hasOneOrManyTemplate.sonModelForeignKey, defaultSonModelForeignKeyValue).update();
    }

    @Override
    public int sync(Record<?, ?> theRecord, RecordList<?, ?> targetRecords, Map<String, String> stringStringMap) {
        return sync(theRecord, getTargetRecordPrimaryKeyIds(targetRecords), stringStringMap);
    }

    @Override
    public int sync(Record<?, ?> theRecord, Collection<String> targetPrimaryKeyValues, Map<String, String> stringStringMap) {
        // 关联键值(当前表关系键(默认当前表主键))(子表外键)
        String relationKeyValue = String.valueOf(theRecord.getMetadataMap().get(hasOneOrManyTemplate.localModelLocalKey).getValue());

        return hasOneOrManyTemplate.sonModel.newQuery().transaction(() -> {
            // 现存的关联关系, 不需要据需存在的, 解除
            int detachNum = hasOneOrManyTemplate.sonModel.newQuery()
                    .whereNotIn(hasOneOrManyTemplate.sonModel.getPrimaryKeyColumnName(), targetPrimaryKeyValues)
                    .where(hasOneOrManyTemplate.sonModelForeignKey, relationKeyValue)
                    .data(hasOneOrManyTemplate.sonModelForeignKey, defaultSonModelForeignKeyValue).update();

            // 执行更新
            int attachNum = hasOneOrManyTemplate.sonModel.newQuery()
                    .whereIn(hasOneOrManyTemplate.sonModel.getPrimaryKeyColumnName(), targetPrimaryKeyValues)
                    .where(hasOneOrManyTemplate.sonModelForeignKey, "!=", relationKeyValue)
                    .data(hasOneOrManyTemplate.sonModelForeignKey, relationKeyValue).update();

            return detachNum + attachNum;
        });
    }

    @Override
    public int toggle(Record<?, ?> theRecord, RecordList<?, ?> targetRecords, Map<String, String> stringStringMap) {
        return toggle(theRecord, getTargetRecordPrimaryKeyIds(targetRecords), stringStringMap);
    }

    @Override
    public int toggle(Record<?, ?> theRecord, Collection<String> targetPrimaryKeyValues, Map<String, String> stringStringMap) {
        if (targetPrimaryKeyValues.isEmpty()) {
            return 0;
        }

        // 关联键值(当前表关系键(默认当前表主键))(子表外键)
        String relationKeyValue = String.valueOf(theRecord.getMetadataMap().get(hasOneOrManyTemplate.localModelLocalKey).getValue());

        return hasOneOrManyTemplate.sonModel.newQuery().transaction(() -> {
            // 现存的关联关系 主键值集合
            List<String> alreadyExistSonModelPrimaryKeyValues = hasOneOrManyTemplate.sonModel.newQuery()
                    .select(hasOneOrManyTemplate.sonModel.getPrimaryKeyColumnName())
                    .whereIn(hasOneOrManyTemplate.sonModel.getPrimaryKeyColumnName(), targetPrimaryKeyValues)
                    .where(hasOneOrManyTemplate.sonModelForeignKey, relationKeyValue)
                    .get().toOneColumnList();

            // 现存的关联关系, 解除关系
            int detachNum = hasOneOrManyTemplate.sonModel.newQuery()
                .whereIn(hasOneOrManyTemplate.sonModel.getPrimaryKeyColumnName(), targetPrimaryKeyValues)
                .where(hasOneOrManyTemplate.sonModelForeignKey, relationKeyValue)
                .data(hasOneOrManyTemplate.sonModelForeignKey, defaultSonModelForeignKeyValue).update();

            // 需要增加的关系 主键值集合
            Collection<String> compatibleTargetPrimaryKeyValues = compatibleCollection(targetPrimaryKeyValues);
            compatibleTargetPrimaryKeyValues.removeAll(alreadyExistSonModelPrimaryKeyValues);

            // 不存在的关系, 新增关系
            int attachNum = !compatibleTargetPrimaryKeyValues.isEmpty() ? hasOneOrManyTemplate.sonModel.newQuery()
                    .whereIn(hasOneOrManyTemplate.sonModel.getPrimaryKeyColumnName(), compatibleTargetPrimaryKeyValues)
                    .data(hasOneOrManyTemplate.sonModelForeignKey, relationKeyValue).update() : 0;

            return detachNum + attachNum;
        });
    }

    static class HasOneOrManyTemplate {
        final Model<?, ?> sonModel;

        final String sonModelForeignKey;

        final String localModelLocalKey;

        HasOneOrManyTemplate(Field field) {
            HasOneOrMany hasOneOrMany = field.getAnnotation(HasOneOrMany.class);
            sonModel = getModelInstance(field);
            sonModelForeignKey = hasOneOrMany.sonModelForeignKey();
            localModelLocalKey = "".equals(hasOneOrMany.localModelLocalKey())
                    ? sonModel.getPrimaryKeyColumnName()
                    : hasOneOrMany.localModelLocalKey();

        }
    }

}
