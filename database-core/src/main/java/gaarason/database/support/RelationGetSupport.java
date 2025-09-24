package gaarason.database.support;

import gaarason.database.contract.eloquent.Builder;
import gaarason.database.contract.eloquent.Record;
import gaarason.database.contract.eloquent.RecordList;
import gaarason.database.contract.eloquent.relation.RelationSubQuery;
import gaarason.database.contract.function.GenerateRecordListFunctionalInterface;
import gaarason.database.contract.function.RecordWrapper;
import gaarason.database.core.Container;
import gaarason.database.lang.Nullable;
import gaarason.database.provider.ModelShadowProvider;
import gaarason.database.util.ObjectUtils;

import java.util.*;

/**
 * 关联关系获取
 * @author xt
 */
public class RelationGetSupport<T, K> extends Container.SimpleKeeper {

    /**
     * 当前结果集
     */
    protected final RecordList<T, K> records;

    /**
     * 是否启用关联关系
     * 在启用时, 需要手动指定(with)才会生效
     * 在不启用时, 即使手动指定(with)也不会生效
     */
    protected final boolean attachedRelationship;

    @Nullable
    protected ModelMember<?, T, K> modelMember;

    /**
     * 基本对象转化
     * @param container 容器
     * @param tkRecord 结果集
     * @param attachedRelationship 是否启用关联关系
     */
    public RelationGetSupport(Container container, Record<T, K> tkRecord, boolean attachedRelationship) {
        super(container);
        List<Record<T, K>> recordList = new ArrayList<>();
        recordList.add(tkRecord);
        this.attachedRelationship = attachedRelationship;
        this.records = RecordFactory.newRecordList(getContainer(), recordList);
    }

    public RelationGetSupport(Container container, RecordList<T, K> records, boolean attachedRelationship) {
        super(container);
        this.attachedRelationship = attachedRelationship;
        this.records = records;
    }

    public T toObject() {
        return toObjectList().get(0);
    }

    /**
     * 转化为对象列表
     * @return 对象列表s
     */
    public List<T> toObjectList() {
        // 查询并处理关联关系数据
        Map<Record.Relation, relationResultData> relationResultMap = dealRelationData(records);
        // 结果收集
        List<T> list = new ArrayList<>();
        for (Record<T, K> theRecord : records) {
            /*
             * 生成对象
             * a. 单表属性的赋值
             * b. 关联关系属性的赋值
             * c. 关联关系操作属性的赋值
             */
            T entity = generateEntity(theRecord, relationResultMap);

            list.add(entity);
        }
        return list;
    }

    /**
     * 生成对象, 并将关系属性结果进行赋值
     * @param record 查询结果集
     * @param relationResultMap 关系查询结果
     */
    protected T generateEntity(Record<T, K> record, Map<Record.Relation, relationResultData> relationResultMap) {
        ModelShadowProvider modelShadow = getModelShadow();
        // 模型信息
        ModelMember<?, T, K> modelMember = getModelMember();
        EntityMember<T, K> entityMember = modelMember.getEntityMember();

        // 实体类的对象 (仅包含基本数据库字段属性)
        T entity = modelShadow.entityAssignment(entityMember.getEntityClass(), record);

        // 赋值关联关系属性
        for (Map.Entry<Record.Relation, relationResultData> entry : relationResultMap.entrySet()) {
            Record.Relation relation = entry.getKey();
            relationResultData relationResultData = entry.getValue();
            FieldRelationMember fieldRelationMember = relationResultData.fieldRelationMember;
            RelationSubQuery relationSubQuery = fieldRelationMember.getRelationSubQuery();

            // 赋值 关联关系统计
            if (relation.relationOperation) {
                // 筛选批量关联查询结果
                Map<String, Object> map = relationSubQuery.filterBatchRecordByRelationOperation(record, relationResultData.records);

                // 目标属性信息
                FieldMember<?> targetFieldMember = entityMember.getFieldMemberByFieldName(relationResultData.targetFieldName);
                // 目标属性赋值 - 统计属性 - 单数
                Object targetFieldValue = map.get(relationResultData.targetFieldName);
                // 类型转化
                Object targetFieldValueDeserialize = targetFieldMember.deserialize(targetFieldValue);
                // 赋值
                targetFieldMember.fieldSet(entity, targetFieldValueDeserialize);
            }
            // 赋值 关联关系
            else {
                // 筛选批量关联查询结果对象
                List<?> objects = relationSubQuery.filterBatchRecord(record, relationResultData.records, relationResultData.objects);

                // 是否是集合
                if (fieldRelationMember.isPlural()) {
                    // 关系属性赋值 - 复数
                    fieldRelationMember.fieldSet(entity, objects);
                } else {
                    // 关系属性赋值 - 单数
                    fieldRelationMember.fieldSet(entity, objects.isEmpty() ? null : objects.get(0));
                }
            }
        }

        return entity;
    }

    /**
     * 查询并处理关联关系数据
     * @param records 当前全量数据
     * @return 每个属性对应的关系查询结果
     */
    protected Map<Record.Relation, relationResultData> dealRelationData(RecordList<?, ?> records) {
        if (ObjectUtils.isEmpty(records) || !attachedRelationship) {
            return Collections.emptyMap();
        }
        // 关联关系的结果集合
        Map<Record.Relation, relationResultData> relationResultMap = new HashMap<>();
        // 原数据 (整体)
        List<Map<String, Object>> metadataList = records.getMetadata();
        // 数据库实体信息
        EntityMember<?, ?> entityMember = getModelMember().getEntityMember();

        /*
         * 手动构造的records, 可能存在不相同的relationMap
         * 此处目前未兼容这种情况
         */
        Map<String, Record.Relation> relationMap = records.get(0).getRelationMap();

        // 循环每一个有效的关联关系数据
        for (Map.Entry<String, Record.Relation> relationEntry : relationMap.entrySet()) {
            // 目标属性名 (并不一定等于 Relation.name)
            String targetFieldName  = relationEntry.getKey();
            // 关联关系信息
            Record.Relation relation = relationEntry.getValue();
            // 是否关联关系操作
            boolean relationOperation = relation.relationOperation;

            // 关联关系属性信息
            FieldRelationMember fieldRelationMember = entityMember.getFieldRelationMemberByFieldName(
                    relation.relationFieldName);

            // 关联关系字段处理
            RelationSubQuery relationSubQuery = fieldRelationMember.getRelationSubQuery();

            // 中间表，查询构造器
            Builder<?, ?, ?> relationBuilder = relationSubQuery.prepareRelationBuilder(metadataList);

            // 中间表数据
            RecordList<?, ?> relationRecords =  relationSubQuery.dealBatchForRelation(relationBuilder);

            // 关联关系统计查询
            if (relationOperation) {
                // 目标表，查询构造器
                Builder<?, ?, ?> targetBuilder = relationSubQuery.prepareTargetBuilderByRelationOperation(metadataList,
                        relationRecords, relation.operationBuilder, relation.customBuilder);

                // 本级关系查询
                RecordList<?, ?> targetRecordList = getTargetRecordsTransferWith(relation.recordWrapper,
                        () -> relationSubQuery.dealBatchForTargetByRelationOperation(targetBuilder, relationRecords));

                // 收集以便返回
                relationResultMap.put(relation, new relationResultData(targetFieldName, fieldRelationMember, targetRecordList, null));
            }
            // 关联关系查询
            else {
                // 目标表，查询构造器
                Builder<?, ?, ?> targetBuilder = relationSubQuery.prepareTargetBuilder(metadataList, relationRecords,
                        relation.operationBuilder, relation.customBuilder);

                // 本级关系查询
                RecordList<?, ?> targetRecordList = getTargetRecordsTransferWith(relation.recordWrapper,
                        () -> relationSubQuery.dealBatchForTarget(targetBuilder, relationRecords));

                // 转化为普通对象, 递归调用
                List<?> objs = targetRecordList.toObjectList();

                // 收集以便返回
                relationResultMap.put(relation, new relationResultData(targetFieldName, fieldRelationMember, targetRecordList, objs));
            }
        }
        return relationResultMap;
    }

    /**
     * 获取 ModelMember
     * @return ModelMember
     */
    public ModelMember<?, T, K> getModelMember() {
        if (modelMember == null) {
            // records 为空, 是不会调用这个方法的~
            modelMember = getModelShadow().get(records.get(0).getModel());
        }
        return modelMember;
    }

    /**
     * 获取结果集, 并传递with属性
     * @param relationshipRecordWith record 实现
     * @param closure 真实业务逻辑实现
     * @return 批量结果集
     */
    protected static RecordList<?, ?> getTargetRecordsTransferWith(RecordWrapper relationshipRecordWith,
            GenerateRecordListFunctionalInterface closure) {
        // 使用复制结果
        RecordList<?, ?> records = closure.execute();
        // 赋值关联关系
        for (Record<?, ?> theRecord : records) {
            relationshipRecordWith.execute(theRecord);
        }
        return records;
    }

    /**
     * Model 信息
     * @return ModelShadow
     */
    protected ModelShadowProvider getModelShadow() {
        return container.getBean(ModelShadowProvider.class);
    }

    /**
     * 关系查询结果
     */
    public static class relationResultData {
        public String targetFieldName;
        public FieldRelationMember fieldRelationMember;
        public RecordList<?, ?> records;
        public List<?> objects;
        public relationResultData(String targetFieldName, FieldRelationMember fieldRelationMember, RecordList<?, ?> records,
                 @Nullable List<?> objects) {
            this.targetFieldName = targetFieldName;
            this.fieldRelationMember = fieldRelationMember;
            this.records = records;
            this.objects = objects == null ? Collections.emptyList() : objects;
        }
    }

}
