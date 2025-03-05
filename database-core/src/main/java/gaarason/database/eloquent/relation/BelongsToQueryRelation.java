package gaarason.database.eloquent.relation;

import gaarason.database.annotation.BelongsTo;
import gaarason.database.appointment.RelationCache;
import gaarason.database.contract.eloquent.Builder;
import gaarason.database.contract.eloquent.Model;
import gaarason.database.contract.eloquent.Record;
import gaarason.database.contract.eloquent.RecordList;
import gaarason.database.contract.function.BuilderWrapper;
import gaarason.database.core.Container;
import gaarason.database.exception.RelationAttachException;
import gaarason.database.lang.Nullable;
import gaarason.database.provider.ModelShadowProvider;
import gaarason.database.util.ObjectUtils;

import java.lang.reflect.Field;
import java.util.*;

/**
 * 反向一对一关系
 * @author xt
 */
public class BelongsToQueryRelation extends BaseRelationSubQuery {

    public final BelongsToTemplate belongsToTemplate;

    /**
     * 是否多态
     */
    protected boolean enableMorph;

    /**
     * 关系键的默认值, 仅在解除关系时使用
     */
    @Nullable
    protected final Object defaultLocalModelForeignKeyValue;

    /**
     * 多态的默认值, 仅在解除关系时使用
     */
    @Nullable
    protected final Object defaultLocalModelMorphValue;

    public BelongsToQueryRelation(Field field, ModelShadowProvider modelShadowProvider, Model<?, ?> model) {
        super(modelShadowProvider, model);
        belongsToTemplate = initTemplate(field);

        defaultLocalModelForeignKeyValue = modelShadowProvider.parseAnyEntityWithCache(field.getDeclaringClass())
            .getFieldMemberByColumnName(belongsToTemplate.localModelForeignKey)
            .getDefaultValue();

        defaultLocalModelMorphValue = enableMorph ?
            modelShadowProvider.parseAnyEntityWithCache(field.getDeclaringClass())
                .getFieldMemberByColumnName(belongsToTemplate.localModelMorphKey)
                .getDefaultValue() : null;
    }

    protected BelongsToTemplate initTemplate(Field field) {
        return new BelongsToTemplate(field);
    }

    @Nullable
    @Override
    public Builder<?, ?> prepareTargetBuilder(List<Map<String, Object>> metadata, RecordList<?, ?> relationRecordList,
        BuilderWrapper<?, ?> operationBuilder, BuilderWrapper<?, ?> customBuilder) {

        Set<Object> objectSet = enableMorph ?
            getColumnInMapList(metadata, belongsToTemplate.localModelForeignKey,
                belongsToTemplate.localModelMorphKey, belongsToTemplate.localModelMorphValue) :
            getColumnInMapList(metadata, belongsToTemplate.localModelForeignKey);

        return ObjectUtils.isEmpty(objectSet) ? null :
            selectFill(customBuilder.execute(ObjectUtils.typeCast(belongsToTemplate.parentModel.newQuery())), belongsToTemplate.parentModel.getEntityClass(), belongsToTemplate.parentModelLocalKey)
                .whereIn(belongsToTemplate.parentModelLocalKey, objectSet);
    }

    @Override
    public RecordList<?, ?> dealBatchForTarget(@Nullable Builder<?, ?> targetBuilder,
        RecordList<?, ?> relationRecordList) {
        if (targetBuilder == null) {
            return emptyRecordList();
        }
        return belongsToTemplate.parentModel.newQuery().setBuilder(ObjectUtils.typeCast(targetBuilder)).get();
    }

    @Override
    public String filterBatchRecordCacheKey(Record<?, ?> theRecord, RecordList<?, ?> targetRecordList) {
        // 父表的外键字段名
        String column = belongsToTemplate.parentModelLocalKey;
        // 本表的关系键值
        Object value = theRecord.getMetadataMap().get(belongsToTemplate.localModelForeignKey);

        return getClass() + "|" +column + "|" + value;
    }

    @Override
    public List<Object> filterBatchRecord(Record<?, ?> theRecord, RecordList<?, ?> targetRecordList,
            RelationCache cache) {
        // 父表的外键字段名
        String column = belongsToTemplate.parentModelLocalKey;
        // 本表的关系键值
        Object value = theRecord.getMetadataMap().get(belongsToTemplate.localModelForeignKey);

        assert value != null;

        return findObjList(targetRecordList.toObjectList(cache), column, value);
    }

    @Override
    public Builder<?, ?> prepareForWhereHas(BuilderWrapper<?, ?> customBuilder) {
        return customBuilder.execute(ObjectUtils.typeCast(belongsToTemplate.parentModel.newQuery()))
            .when(enableMorph, builder -> builder.where(belongsToTemplate.localModelMorphKey,
                belongsToTemplate.localModelMorphValue))
            .whereColumn(localModel.getTableName() + "." + belongsToTemplate.localModelForeignKey,
                belongsToTemplate.parentModel.getTableName() + "." + belongsToTemplate.parentModelLocalKey);
    }

    @Override
    public int attach(Record<?, ?> theRecord, RecordList<?, ?> targetRecords, Map<String, Object> relationDataMap) {
        if (targetRecords.isEmpty()) {
            return 0;
        }

        // 目标表(父表)model的关联键值
        Object parentModelLocalKeyValue = parentModelLocalKeyValue(targetRecords);

        // 执行更新, 自我更新需要手动刷新属性
        return attachAndRefresh(theRecord, parentModelLocalKeyValue);
    }

    @Override
    public int attach(Record<?, ?> theRecord, Collection<Object> targetPrimaryKeyValues,
        Map<String, Object> relationDataMap) {
        if (targetPrimaryKeyValues.isEmpty()) {
            return 0;
        } else if (targetPrimaryKeyValues.size() > 1) {
            throw new RelationAttachException("The relationship \"@BelongsTo\" could only attach a relationship with " +
                "one, but now more than one.");
        }

        // 集合兼容处理
        final Collection<Object> compatibleTargetPrimaryKeyValues = compatibleCollection(targetPrimaryKeyValues,
            belongsToTemplate.parentModel);

        // 目标表(父表)model的主键
        Object targetPrimaryKeyValue = compatibleTargetPrimaryKeyValues.toArray()[0];

        return theRecord.getModel().newQuery().transaction(() -> {
            // 目标表(父表)model的关联键值
            Object parentModelLocalKeyValue = belongsToTemplate.parentModel.newQuery()
                .select(belongsToTemplate.parentModelLocalKey)
                .where(belongsToTemplate.parentModel.getPrimaryKeyColumnName(), targetPrimaryKeyValue)
                .firstOrFail()
                .getMetadataMap()
                .get(belongsToTemplate.parentModelLocalKey);

            // 执行更新, 自我更新需要手动刷新属性
            assert parentModelLocalKeyValue != null;
            return attachAndRefresh(theRecord, parentModelLocalKeyValue);
        });
    }

    @Override
    public int detach(Record<?, ?> theRecord) {
        return detach(theRecord, Collections.singletonList(
            Objects.requireNonNull(theRecord.getMetadataMap().get(belongsToTemplate.localModelForeignKey)).toString()));
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

        // 集合兼容处理
        final Collection<Object> compatibleTargetPrimaryKeyValues = compatibleCollection(targetPrimaryKeyValues,
            belongsToTemplate.parentModel);

        // 执行更新, 自我更新需要手动刷新属性
        // 目标,必须是关联关系, 才解除
        // 解除可以多个
        int successNum = theRecord.getModel()
            .newQuery()
            .where(theRecord.getModel().getPrimaryKeyColumnName(),
                String.valueOf(theRecord.getOriginalPrimaryKeyValue()))
            .whereIn(belongsToTemplate.localModelForeignKey,
                (builder -> builder.select(belongsToTemplate.parentModelLocalKey)
                    .from(belongsToTemplate.parentModel.getTableName())
                    .whereIn(belongsToTemplate.parentModel.getPrimaryKeyColumnName(),
                        compatibleTargetPrimaryKeyValues)))
            .when(enableMorph,
                builder -> builder.where(belongsToTemplate.localModelMorphKey, belongsToTemplate.localModelMorphValue))
            .data(belongsToTemplate.localModelForeignKey, defaultLocalModelForeignKeyValue)
            .when(enableMorph,
                builder -> builder.data(belongsToTemplate.localModelMorphKey, defaultLocalModelMorphValue))
            .update();
        if (successNum > 0) {
            Map<String, Object> metadataMap = theRecord.getMetadataMap();
            metadataMap.put(belongsToTemplate.localModelForeignKey, defaultLocalModelForeignKeyValue);
            if (enableMorph) {
                metadataMap.put(belongsToTemplate.localModelMorphKey, defaultLocalModelMorphValue);
            }
            theRecord.refresh(metadataMap);
        }
        return successNum;
    }

    @Override
    public int sync(Record<?, ?> theRecord, RecordList<?, ?> targetRecords, Map<String, Object> relationDataMap) {
        return attach(theRecord, targetRecords, relationDataMap);
    }

    @Override
    public int sync(Record<?, ?> theRecord, Collection<Object> targetPrimaryKeyValues,
        Map<String, Object> relationDataMap) {
        return attach(theRecord, targetPrimaryKeyValues, relationDataMap);
    }

    @Override
    public int toggle(Record<?, ?> theRecord, RecordList<?, ?> targetRecords, Map<String, Object> relationDataMap) {
        if (targetRecords.isEmpty()) {
            return 0;
        }

        // 目标表(父表)model的关联键值
        Object parentModelLocalKeyValue = parentModelLocalKeyValue(targetRecords);

        // 关系已经存在, 切换就是解除
        if (Objects.equals(theRecord.getMetadataMap().get(belongsToTemplate.localModelForeignKey),
            parentModelLocalKeyValue)) {
            return detach(theRecord, Collections.singletonList(parentModelLocalKeyValue));
        }
        // 关系不存在, 切换就是增加
        else {
            return attachAndRefresh(theRecord, parentModelLocalKeyValue);
        }

    }

    @Override
    public int toggle(Record<?, ?> theRecord, Collection<Object> targetPrimaryKeyValues,
        Map<String, Object> relationDataMap) {
        if (targetPrimaryKeyValues.isEmpty()) {
            return 0;
        } else if (targetPrimaryKeyValues.size() > 1) {
            throw new RelationAttachException("The relationship \"@BelongsTo\" could only toggle a relationship with " +
                "one, but now more than one.");
        }

        // 集合兼容处理
        final Collection<Object> compatibleTargetPrimaryKeyValues = compatibleCollection(targetPrimaryKeyValues,
            belongsToTemplate.parentModel);

        // 目标表(父表)model的主键
        Object targetPrimaryKeyValue = compatibleTargetPrimaryKeyValues.toArray()[0];

        return belongsToTemplate.parentModel.newQuery().transaction(() -> {

            // 目标表(父表)model的关联键值
            Object parentModelLocalKeyValue = belongsToTemplate.parentModel.newQuery()
                .select(belongsToTemplate.parentModelLocalKey)
                .where(belongsToTemplate.parentModel.getPrimaryKeyColumnName(), targetPrimaryKeyValue)
                .firstOrFail()
                .getMetadataMap()
                .get(belongsToTemplate.parentModelLocalKey);

            // 关系已经存在, 切换就是解除
            if (Objects.equals(theRecord.getMetadataMap().get(belongsToTemplate.localModelForeignKey),
                parentModelLocalKeyValue) && (!enableMorph ||
                Objects.equals(theRecord.getMetadataMap().get(belongsToTemplate.localModelMorphKey),
                    belongsToTemplate.localModelMorphValue))) {
                return detach(theRecord, compatibleTargetPrimaryKeyValues);
            }
            // 关系不存在, 切换就是增加
            else {
                assert parentModelLocalKeyValue != null;
                return attachAndRefresh(theRecord, parentModelLocalKeyValue);
            }
        });
    }

    protected Object parentModelLocalKeyValue(RecordList<?, ?> targetRecords) {
        if (targetRecords.size() > 1) {
            throw new RelationAttachException(
                "The relationship \"@BelongsTo\" could only attach/toggle/sync a relationship with " +
                    "one, but now more than one.");
        }

        Map<String, Object> metadataMap = targetRecords.get(0).getMetadataMap();
        if (!metadataMap.containsKey(belongsToTemplate.parentModelLocalKey)) {
            throw new RelationAttachException(
                "Not found the relation key[" + belongsToTemplate.parentModelLocalKey + "] in the target records.");
        }
        Object value = metadataMap.get(belongsToTemplate.parentModelLocalKey);
        if (null == value) {
            throw new RelationAttachException("The relation key[" + belongsToTemplate.parentModelLocalKey +
                "] in the target records should not be NULL.");
        }

        // 目标表(父表)model的关联键值
        return value;
    }

    /**
     * 增加关系并刷新自身
     * @param theRecord 本模型
     * @param localModelForeignKeyValue 父模型的关系键(本模型外键)
     * @return 受影响的行数
     */
    protected int attachAndRefresh(Record<?, ?> theRecord, Object localModelForeignKeyValue) {
        // 执行更新, 自我更新需要手动刷新属性
        int successNum = theRecord.getModel()
            .newQuery()
            .where(theRecord.getModel().getPrimaryKeyColumnName(),
                String.valueOf(theRecord.getOriginalPrimaryKeyValue()))
            .andWhere(builder -> builder.where(belongsToTemplate.localModelForeignKey, "!=", localModelForeignKeyValue)
                .when(enableMorph, builder1 -> builder1.orWhere(
                    builder2 -> builder2.where(belongsToTemplate.localModelMorphKey, "!=",
                        belongsToTemplate.localModelMorphValue))))
            .data(belongsToTemplate.localModelForeignKey, localModelForeignKeyValue)
            .when(enableMorph,
                builder -> builder.data(belongsToTemplate.localModelMorphKey, belongsToTemplate.localModelMorphValue))
            .update();
        if (successNum > 0) {
            Map<String, Object> metadataMap = theRecord.getMetadataMap();
            metadataMap.put(belongsToTemplate.localModelForeignKey, localModelForeignKeyValue);
            theRecord.refresh(metadataMap);
        }
        return successNum;
    }

    @Override
    public Container getContainer() {
        return belongsToTemplate.parentModel.getGaarasonDataSource().getContainer();
    }

    public class BelongsToTemplate {

        final public Model<?, ?> parentModel;

        final public String localModelForeignKey;

        final public String parentModelLocalKey;

        final public String localModelMorphKey;

        final public String localModelMorphValue;

        public BelongsToTemplate(Field field) {
            BelongsTo belongsTo = field.getAnnotation(BelongsTo.class);
            parentModel = getModelInstance(field);
            localModelForeignKey = belongsTo.localModelForeignKey();
            parentModelLocalKey = belongsTo.parentModelLocalKey().isEmpty() ? getPrimaryKeyColumnName(parentModel) :
                belongsTo.parentModelLocalKey();
            localModelMorphKey = belongsTo.localModelMorphKey();
            localModelMorphValue = belongsTo.localModelMorphValue().isEmpty() ? parentModel.getTableName() :
                belongsTo.localModelMorphValue();
            enableMorph = !localModelMorphKey.isEmpty();
        }

        public BelongsToTemplate(Model<?, ?> parentModel, String localModelForeignKey, String parentModelLocalKey,
                String localModelMorphKey, String localModelMorphValue) {
            this.parentModel = parentModel;
            this.localModelForeignKey = localModelForeignKey;
            this.parentModelLocalKey = parentModelLocalKey;
            this.localModelMorphKey = localModelMorphKey;
            this.localModelMorphValue = localModelMorphValue;
            enableMorph = !localModelMorphKey.isEmpty();
        }
    }
}