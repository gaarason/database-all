package gaarason.database.eloquent.relation;

import gaarason.database.annotation.HasOneOrMany;
import gaarason.database.appointment.RelationCache;
import gaarason.database.contract.eloquent.Builder;
import gaarason.database.contract.eloquent.Model;
import gaarason.database.contract.eloquent.Record;
import gaarason.database.contract.eloquent.RecordList;
import gaarason.database.contract.function.BuilderWrapper;
import gaarason.database.contract.query.Grammar;
import gaarason.database.core.Container;
import gaarason.database.lang.Nullable;
import gaarason.database.provider.ModelShadowProvider;
import gaarason.database.util.ObjectUtils;
import gaarason.database.util.StringUtils;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 一对一/一对多关系
 * @author xt
 */
public class HasOneOrManyQueryRelation extends BaseRelationSubQuery {

    protected final HasOneOrManyTemplate hasOneOrManyTemplate;

    /**
     * 是否多态
     */
    protected boolean enableMorph;

    /**
     * 目标模型外键的默认值, 仅在解除关系时使用
     */
    @Nullable
    protected final Object defaultSonModelForeignKeyValue;

    /**
     * 目标模型多态的默认值, 仅在解除关系时使用
     */
    @Nullable
    protected final Object defaultSonModelMorphValue;

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
    public Builder<?, ?> prepareTargetBuilder(List<Map<String, Object>> metadata,
        RecordList<?, ?> relationRecordList, BuilderWrapper<?, ?> operationBuilder,
        BuilderWrapper<?, ?> customBuilder) {

        // 查询构造器包装
        Builder<?, ?> queryBuilder = customBuilder.execute(
            ObjectUtils.typeCast(hasOneOrManyTemplate.sonModel.newQuery()));

        setWhere(metadata, queryBuilder);

//        return queryBuilder.select(hasOneOrManyTemplate.sonModel.getEntityClass());
        return selectFill(queryBuilder, hasOneOrManyTemplate.sonModel.getEntityClass(), hasOneOrManyTemplate.sonModelForeignKey);
    }

    @Override
    public Builder<?, ?> prepareTargetBuilderByRelationOperation(List<Map<String, Object>> metadata,
        RecordList<?, ?> relationRecordList, BuilderWrapper<?, ?> operationBuilder,
        BuilderWrapper<?, ?> customBuilder) {

        // 查询构造器包装
        Builder<?, ?> queryBuilder = customBuilder.execute(
            ObjectUtils.typeCast(hasOneOrManyTemplate.sonModel.newQuery()));

        Grammar grammar = queryBuilder.getGrammar();
        if (!grammar.isEmpty(Grammar.SQLPartType.GROUP)) {
            String alias = StringUtils.getRandomString(6);
            if (grammar.isEmpty(Grammar.SQLPartType.SELECT)) {
                Grammar.SQLPartInfo groupInfo = grammar.get(Grammar.SQLPartType.GROUP);
                queryBuilder.selectRaw(groupInfo.getSqlString(), groupInfo.getParameters())
                    .select(hasOneOrManyTemplate.sonModelForeignKey);
            }
            setWhere(metadata, queryBuilder);
            Builder<?, ?> finalQueryBuilder = queryBuilder;
            queryBuilder = hasOneOrManyTemplate.sonModel.newQuery()
                .from(alias + "sub", subBuilder -> ObjectUtils.typeCast(finalQueryBuilder));
        }else {
            setWhere(metadata, queryBuilder);
        }

        // 操作响应包装
        queryBuilder = operationBuilder.execute(ObjectUtils.typeCast(queryBuilder));

        return queryBuilder.select(hasOneOrManyTemplate.sonModelForeignKey)
            .group(hasOneOrManyTemplate.sonModelForeignKey);
    }

    @Override
    public RecordList<?, ?> dealBatchForTarget(@Nullable Builder<?, ?> targetBuilder,
        RecordList<?, ?> relationRecordList) {
        if (targetBuilder == null) {
            return emptyRecordList();
        }
        return hasOneOrManyTemplate.sonModel.newQuery().setBuilder(ObjectUtils.typeCast(targetBuilder)).get();
    }

    @Override
    public RecordList<?, ?> dealBatchForTargetByRelationOperation(@Nullable Builder<?, ?> targetBuilder,
        RecordList<?, ?> relationRecordList) {
        return dealBatchForTarget(targetBuilder, relationRecordList);
    }

    @Override
    public Map<String, Object> filterBatchRecordByRelationOperation(Record<?, ?> theRecord,
        RecordList<?, ?> targetRecordList, RelationCache cache) {
        // 子表的外键字段名
        String column = hasOneOrManyTemplate.sonModelForeignKey;
        // 本表的关系键值
        Object value = theRecord.getMetadataMap().get(hasOneOrManyTemplate.localModelLocalKey);

        return findObj(targetRecordList.getMetadata(), column, value);
    }

    @Override
    public String filterBatchRecordCacheKey(Record<?, ?> theRecord, RecordList<?, ?> targetRecordList) {
        // 子表的外键字段名
        String column = hasOneOrManyTemplate.sonModelForeignKey;
        // 本表的关系键值
        Object value = theRecord.getMetadataMap().get(hasOneOrManyTemplate.localModelLocalKey);

        return getClass() + "|" +column + "|" + value;
    }
    @Override
    public List<Object> filterBatchRecord(Record<?, ?> theRecord, RecordList<?, ?> targetRecordList,
            RelationCache cache) {
        // 子表的外键字段名
        String column = hasOneOrManyTemplate.sonModelForeignKey;
        // 本表的关系键值
        Object value = theRecord.getMetadataMap().get(hasOneOrManyTemplate.localModelLocalKey);

        assert value != null;

        return findObjList(targetRecordList.toObjectList(cache), column, value);
    }

    @Override
    public Builder<?, ?> prepareForWhereHas(BuilderWrapper<?, ?> customBuilder) {
        return customBuilder.execute(ObjectUtils.typeCast(hasOneOrManyTemplate.sonModel.newQuery()))
            .when(enableMorph, builder -> builder.where(hasOneOrManyTemplate.sonModelMorphKey,
                hasOneOrManyTemplate.sonModelMorphValue))
            .whereColumn(localModel.getTableName() +"."+hasOneOrManyTemplate.localModelLocalKey,
                hasOneOrManyTemplate.sonModel.getTableName()+"."+hasOneOrManyTemplate.sonModelForeignKey);
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

    /**
     * 查询构造器条件设置
     * @param metadata 当前recordList的元数据
     * @param queryBuilder 查询构造器
     * @param <T> 实体类
     * @param <K> 主键类
     * @return 查询构造器
     */
    protected <T, K> Builder<T, K> setWhere(List<Map<String, Object>> metadata, Builder<T, K> queryBuilder) {
        return queryBuilder.whereIn(hasOneOrManyTemplate.sonModelForeignKey,
                getColumnInMapList(metadata, hasOneOrManyTemplate.localModelLocalKey))
            .when(enableMorph, builder -> builder.where(hasOneOrManyTemplate.sonModelMorphKey,
                hasOneOrManyTemplate.sonModelMorphValue));
    }

    @Override
    public Container getContainer() {
        return hasOneOrManyTemplate.sonModel.getGaarasonDataSource().getContainer();
    }

    public class HasOneOrManyTemplate {

        final public Model<?, ?> sonModel;

        final public String sonModelForeignKey;

        final public String localModelLocalKey;

        final public String sonModelMorphKey;

        final public String sonModelMorphValue;

        HasOneOrManyTemplate(Field field) {
            HasOneOrMany hasOneOrMany = field.getAnnotation(HasOneOrMany.class);
            sonModel = getModelInstance(field);
            sonModelForeignKey = hasOneOrMany.sonModelForeignKey();
            localModelLocalKey = hasOneOrMany.localModelLocalKey().isEmpty() ? getPrimaryKeyColumnName(sonModel) :
                hasOneOrMany.localModelLocalKey();
            sonModelMorphKey = hasOneOrMany.sonModelMorphKey();
            sonModelMorphValue = hasOneOrMany.sonModelMorphValue().isEmpty() ? localModel.getTableName() :
                hasOneOrMany.sonModelMorphValue();
            enableMorph = !sonModelMorphKey.isEmpty();
        }
    }

}
