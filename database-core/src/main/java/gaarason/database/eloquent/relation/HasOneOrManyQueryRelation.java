package gaarason.database.eloquent.relation;

import gaarason.database.annotation.HasOneOrMany;
import gaarason.database.contract.eloquent.Builder;
import gaarason.database.contract.eloquent.Model;
import gaarason.database.contract.eloquent.Record;
import gaarason.database.contract.eloquent.RecordList;
import gaarason.database.contract.function.GenerateSqlPartFunctionalInterface;
import gaarason.database.core.Container;
import gaarason.database.lang.Nullable;
import gaarason.database.provider.ModelShadowProvider;
import gaarason.database.support.RecordFactory;
import gaarason.database.util.ObjectUtils;

import java.lang.reflect.Field;
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
     * 是否多态
     */
    private boolean enableMorph;

    /**
     * 目标模型外键的默认值, 仅在解除关系时使用
     */
    @Nullable
    private final Object defaultSonModelForeignKeyValue;

    /**
     * 目标模型多态的默认值, 仅在解除关系时使用
     */
    @Nullable
    private final Object defaultSonModelMorphValue;

    public HasOneOrManyQueryRelation(Field field, ModelShadowProvider modelShadowProvider, Model<?, ?> model) {
        super(modelShadowProvider, model);
        hasOneOrManyTemplate = new HasOneOrManyTemplate(field);

        defaultSonModelForeignKeyValue = modelShadowProvider.get(hasOneOrManyTemplate.sonModel)
            .getEntityMember()
            .getFieldMemberByColumnName(hasOneOrManyTemplate.sonModelForeignKey)
            .getDefaultValue();

        defaultSonModelMorphValue = enableMorph ? modelShadowProvider.get(hasOneOrManyTemplate.sonModel)
            .getEntityMember()
            .getFieldMemberByColumnName(hasOneOrManyTemplate.sonModelMorphKey)
            .getDefaultValue() : null;
    }

    @Override
    public Builder<?, ?>[] prepareBuilderArr(boolean relationOperation, List<Map<String, Object>> originalMetadataMapList,
        GenerateSqlPartFunctionalInterface<?, ?> generateSqlPart) {
        return new Builder<?, ?>[]{null,
            generateSqlPart.execute(ObjectUtils.typeCast(hasOneOrManyTemplate.sonModel.newQuery()))
                // 关联关系操作, 则指定查询外键以及group
                .when(relationOperation, builder -> builder.select(hasOneOrManyTemplate.sonModelForeignKey).group(hasOneOrManyTemplate.sonModelForeignKey))
                .when(!relationOperation, builder -> builder.select(hasOneOrManyTemplate.sonModel.getEntityClass()))
                .whereIn(hasOneOrManyTemplate.sonModelForeignKey,
                    getColumnInMapList(originalMetadataMapList, hasOneOrManyTemplate.localModelLocalKey))
                .when(enableMorph,
                    builder -> builder.where(hasOneOrManyTemplate.sonModelMorphKey, hasOneOrManyTemplate.sonModelMorphValue))};
    }

    @Override
    public RecordList<?, ?> dealBatchForTarget(boolean relationOperation, @Nullable Builder<?, ?> builderForTarget, RecordList<?, ?> relationRecordList) {
        if(builderForTarget == null){
            return RecordFactory.newRecordList(getContainer());
        }
        return hasOneOrManyTemplate.sonModel.newQuery().setBuilder(ObjectUtils.typeCast(builderForTarget)).get();
    }

    @Override
    public List<Object> filterBatchRecord(boolean relationOperation, Record<?, ?> theRecord, RecordList<?, ?> targetRecordList,
        Map<String, RecordList<?, ?>> cacheRelationRecordList) {
        // 子表的外键字段名
        String column = hasOneOrManyTemplate.sonModelForeignKey;
        // 本表的关系键值
        Object value = theRecord.getMetadataMap().get(hasOneOrManyTemplate.localModelLocalKey);

        assert value != null;

        if(relationOperation) {
            return findObj(targetRecordList.getOriginalMetadataMapList(),column,value);
        }

        return findObjList(targetRecordList.toObjectList(cacheRelationRecordList), column, value);
    }

    @Override
    public int attach(Record<?, ?> theRecord, RecordList<?, ?> targetRecords, Map<String, Object> relationDataMap) {
        return attach(theRecord, getTargetRecordPrimaryKeyIds(targetRecords), relationDataMap);
    }

    @Override
    public int attach(Record<?, ?> theRecord, Collection<Object> targetPrimaryKeyValues,
        Map<String, Object> relationDataMap) {
        if (targetPrimaryKeyValues.isEmpty()) {
            return 0;
        }

        // 关联键值(当前表关系键(默认当前表主键))(子表外键)
        Object relationKeyValue = theRecord.getMetadataMap().get(hasOneOrManyTemplate.localModelLocalKey);

        // 执行更新
        assert relationKeyValue != null;
        return hasOneOrManyTemplate.sonModel.newQuery()
            .whereIn(hasOneOrManyTemplate.sonModel.getPrimaryKeyColumnName(), targetPrimaryKeyValues)
            .andWhere(builder -> builder.where(hasOneOrManyTemplate.sonModelForeignKey, "!=", relationKeyValue)
                .when(enableMorph, builder1 -> builder1.orWhere(
                    builder2 -> builder2.where(hasOneOrManyTemplate.sonModelMorphKey, "!=",
                        hasOneOrManyTemplate.sonModelMorphValue))))
            .data(hasOneOrManyTemplate.sonModelForeignKey, relationKeyValue)
            .when(enableMorph,
                builder -> builder.data(hasOneOrManyTemplate.sonModelMorphKey, hasOneOrManyTemplate.sonModelMorphValue))
            .update();
    }

    @Override
    public int detach(Record<?, ?> theRecord) {
        // 关联键值(当前表关系键(默认当前表主键))(子表外键)
        Object relationKeyValue = theRecord.getMetadataMap().get(hasOneOrManyTemplate.localModelLocalKey);

        // 执行更新
        // 目标, 必须是关联关系, 才解除
        return hasOneOrManyTemplate.sonModel.newQuery()
            .where(hasOneOrManyTemplate.sonModelForeignKey, relationKeyValue)
            .when(enableMorph, builder -> builder.where(hasOneOrManyTemplate.sonModelMorphKey,
                hasOneOrManyTemplate.sonModelMorphValue))
            .data(hasOneOrManyTemplate.sonModelForeignKey, defaultSonModelForeignKeyValue)
            .when(enableMorph,
                builder -> builder.data(hasOneOrManyTemplate.sonModelMorphKey, defaultSonModelMorphValue))
            .update();
    }

    @Override
    public int detach(Record<?, ?> theRecord, RecordList<?, ?> targetRecords) {
        // 应该更新的子表的主键列表
        List<Object> targetRecordPrimaryKeyIds = targetRecords.toList(
            recordTemp -> recordTemp.getMetadataMap().get(recordTemp.getModel().getPrimaryKeyColumnName()));
        return detach(theRecord, targetRecordPrimaryKeyIds);
    }

    @Override
    public int detach(Record<?, ?> theRecord, Collection<Object> targetPrimaryKeyValues) {
        if (targetPrimaryKeyValues.isEmpty()) {
            return 0;
        }

        // 关联键值(当前表关系键(默认当前表主键))(子表外键)
        Object relationKeyValue = theRecord.getMetadataMap().get(hasOneOrManyTemplate.localModelLocalKey);

        // 执行更新
        // 目标,必须是关联关系, 才解除
        return hasOneOrManyTemplate.sonModel.newQuery()
            .whereIn(hasOneOrManyTemplate.sonModel.getPrimaryKeyColumnName(), targetPrimaryKeyValues)
            .where(hasOneOrManyTemplate.sonModelForeignKey, relationKeyValue)
            .when(enableMorph, builder -> builder.where(hasOneOrManyTemplate.sonModelMorphKey,
                hasOneOrManyTemplate.sonModelMorphValue))
            .data(hasOneOrManyTemplate.sonModelForeignKey, defaultSonModelForeignKeyValue)
            .when(enableMorph,
                builder -> builder.data(hasOneOrManyTemplate.sonModelMorphKey, defaultSonModelMorphValue))
            .update();
    }

    @Override
    public int sync(Record<?, ?> theRecord, RecordList<?, ?> targetRecords, Map<String, Object> relationDataMap) {
        return sync(theRecord, getTargetRecordPrimaryKeyIds(targetRecords), relationDataMap);
    }

    @Override
    public int sync(Record<?, ?> theRecord, Collection<Object> targetPrimaryKeyValues,
        Map<String, Object> relationDataMap) {
        // 关联键值(当前表关系键(默认当前表主键))(子表外键)
        Object relationKeyValue = theRecord.getMetadataMap().get(hasOneOrManyTemplate.localModelLocalKey);
        assert relationKeyValue != null;

        return hasOneOrManyTemplate.sonModel.newQuery().transaction(() -> {
            // 现存的关联关系, 不需要据需存在的, 解除
            int detachNum = hasOneOrManyTemplate.sonModel.newQuery()
                .whereNotIn(hasOneOrManyTemplate.sonModel.getPrimaryKeyColumnName(), targetPrimaryKeyValues)
                .where(hasOneOrManyTemplate.sonModelForeignKey, relationKeyValue)
                .when(enableMorph, builder -> builder.where(hasOneOrManyTemplate.sonModelMorphKey,
                    hasOneOrManyTemplate.sonModelMorphValue))
                .data(hasOneOrManyTemplate.sonModelForeignKey, defaultSonModelForeignKeyValue)
                .when(enableMorph,
                    builder -> builder.data(hasOneOrManyTemplate.sonModelMorphKey, defaultSonModelMorphValue))
                .update();

            // 执行更新
            int attachNum = attach(theRecord, targetPrimaryKeyValues, relationDataMap);

            return detachNum + attachNum;
        });
    }

    @Override
    public int toggle(Record<?, ?> theRecord, RecordList<?, ?> targetRecords, Map<String, Object> relationDataMap) {
        return toggle(theRecord, getTargetRecordPrimaryKeyIds(targetRecords), relationDataMap);
    }

    @Override
    public int toggle(Record<?, ?> theRecord, Collection<Object> targetPrimaryKeyValues,
        Map<String, Object> relationDataMap) {
        if (targetPrimaryKeyValues.isEmpty()) {
            return 0;
        }

        // 关联键值(当前表关系键(默认当前表主键))(子表外键)
        Object relationKeyValue = theRecord.getMetadataMap().get(hasOneOrManyTemplate.localModelLocalKey);

        return hasOneOrManyTemplate.sonModel.newQuery().transaction(() -> {
            // 现存的关联关系 主键值集合
            List<Object> alreadyExistSonModelPrimaryKeyValues = hasOneOrManyTemplate.sonModel.newQuery()
                .select(hasOneOrManyTemplate.sonModel.getPrimaryKeyColumnName())
                .whereIn(hasOneOrManyTemplate.sonModel.getPrimaryKeyColumnName(), targetPrimaryKeyValues)
                .where(hasOneOrManyTemplate.sonModelForeignKey, relationKeyValue)
                .when(enableMorph, builder -> builder.where(hasOneOrManyTemplate.sonModelMorphKey,
                    hasOneOrManyTemplate.sonModelMorphValue))
                .get()
                .toOneColumnList();

            // 现存的关联关系, 解除关系
            int detachNum = detach(theRecord, targetPrimaryKeyValues);

            // 需要增加的关系 主键值集合
            Collection<Object> compatibleTargetPrimaryKeyValues = compatibleCollection(targetPrimaryKeyValues,
                hasOneOrManyTemplate.sonModel);
            compatibleTargetPrimaryKeyValues.removeAll(alreadyExistSonModelPrimaryKeyValues);

            // 不存在的关系, 新增关系
            int attachNum = !compatibleTargetPrimaryKeyValues.isEmpty() ?
                attach(theRecord, compatibleTargetPrimaryKeyValues, relationDataMap) : 0;

            return detachNum + attachNum;
        });
    }

    @Override
    protected Container getContainer() {
        return hasOneOrManyTemplate.sonModel.getGaarasonDataSource().getContainer();
    }

    class HasOneOrManyTemplate {

        final Model<?, ?> sonModel;

        final String sonModelForeignKey;

        final String localModelLocalKey;

        final String sonModelMorphKey;

        final String sonModelMorphValue;

        HasOneOrManyTemplate(Field field) {
            HasOneOrMany hasOneOrMany = field.getAnnotation(HasOneOrMany.class);
            sonModel = getModelInstance(field);
            sonModelForeignKey = hasOneOrMany.sonModelForeignKey();
            localModelLocalKey = "".equals(hasOneOrMany.localModelLocalKey()) ? getPrimaryKeyColumnName(sonModel) :
                hasOneOrMany.localModelLocalKey();
            sonModelMorphKey = hasOneOrMany.sonModelMorphKey();
            sonModelMorphValue = "".equals(hasOneOrMany.sonModelMorphValue()) ? localModel.getTableName() :
                hasOneOrMany.sonModelMorphValue();
            enableMorph = !"".equals(sonModelMorphKey);
        }
    }

}
