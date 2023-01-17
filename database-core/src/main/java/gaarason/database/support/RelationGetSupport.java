package gaarason.database.support;

import gaarason.database.appointment.SqlType;
import gaarason.database.contract.eloquent.Builder;
import gaarason.database.contract.eloquent.Record;
import gaarason.database.contract.eloquent.RecordList;
import gaarason.database.contract.eloquent.relation.RelationSubQuery;
import gaarason.database.contract.function.GenerateRecordListFunctionalInterface;
import gaarason.database.contract.function.GenerateSqlPartFunctionalInterface;
import gaarason.database.contract.function.RelationshipRecordWithFunctionalInterface;
import gaarason.database.core.Container;
import gaarason.database.lang.Nullable;
import gaarason.database.provider.ModelShadowProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public RelationGetSupport(Container container, List<Record<T, K>> records, boolean attachedRelationship) {
        super(container);
        this.attachedRelationship = attachedRelationship;
        this.records = RecordFactory.newRecordList(getContainer(), records);
    }

    public RelationGetSupport(Container container, RecordList<T, K> records, boolean attachedRelationship) {
        super(container);
        this.attachedRelationship = attachedRelationship;
        this.records = records;
    }

    public T toObject() {
        return toObjectList().get(0);
    }

    public T toObject(Map<String, RecordList<?, ?>> cacheRelationRecordList) {
        return toObjectList(cacheRelationRecordList).get(0);
    }

    public List<T> toObjectList() {
        Map<String, RecordList<?, ?>> cacheRelationRecordList = new HashMap<>();
        return toObjectList(cacheRelationRecordList);
    }

    /**
     * 转化为对象列表
     * @param cacheRecords 结果集缓存(用于优化递归算法)
     * @return 对象列表
     */
    public List<T> toObjectList(Map<String, RecordList<?, ?>> cacheRecords) {
        // 同级数据源
        List<Map<String, Object>> originalMetadataMapList = records.getOriginalMetadataMapList();
        ModelShadowProvider modelShadow = getModelShadow();

        List<T> list = new ArrayList<>();
        // 关联关系的临时性缓存
        for (Record<T, K> theRecord : records) {
            // 生成对象
            T entity = generateEntity(theRecord, cacheRecords, originalMetadataMapList, modelShadow);
            list.add(entity);
        }
        return list;
    }

    /**
     * 生成对象
     * @param record 查询结果集
     * @param cacheRecords 结果集缓存(用于优化递归算法)
     * @param originalMetadataMapList 同级数据源
     * @param modelShadow Model信息大全
     */
    protected T generateEntity(Record<T, K> record, Map<String, RecordList<?, ?>> cacheRecords,
        List<Map<String, Object>> originalMetadataMapList, ModelShadowProvider modelShadow) {

        // 模型信息
        ModelMember<T, K> modelMember = modelShadow.get(records.get(0).getModel());
        EntityMember<T, K> entityMember = modelMember.getEntityMember();

        // 实体类的对象 (仅包含数据库字段属性)
        T entity = modelShadow.entityAssignment(entityMember.getEntityClass(), record);

        // 处理关联关系属性
        dealRelationField(entity, record, entityMember, cacheRecords, originalMetadataMapList);

        // 处理关联关系"操作"属性
        dealRelationOperationField();

        return entity;
    }

    private void dealRelationOperationField(T entity, Record<T, K> record, EntityMember<T, K> entityMember, Map<String, RecordList<?, ?>> cacheRecords, List<Map<String, Object>> originalMetadataMapList) {
        Map<String, GenerateSqlPartFunctionalInterface<?, ?>> relationOperationFieldMap = record.getRelationOperationBuilderMap();

        // 关联关系属性集合
        Map<String, FieldRelationMember> relationFieldMap = entityMember.getRelationFieldMap();

        for (Map.Entry<String, GenerateSqlPartFunctionalInterface<?, ?>> entry : relationOperationFieldMap.entrySet()) {
            FieldRelationMember fieldRelationMember = relationFieldMap.get(entry.getKey());



        }
    }

    /**
     * 处理entity的关联关系属性
     * @param entity 实体对象
     * @param record 查询结果集
     * @param entityMember 数据库实体信息
     * @param cacheRecords 结果集缓存(用于优化递归算法)
     * @param originalMetadataMapList 同级数据源
     */
    private void dealRelationField(T entity, Record<T, K> record, EntityMember<T, K> entityMember, Map<String, RecordList<?, ?>> cacheRecords, List<Map<String, Object>> originalMetadataMapList){

        // 关联关系属性集合
        Map<String, FieldRelationMember> relationFieldMap = entityMember.getRelationFieldMap();

        // 遍历关联关系属性
        for (Map.Entry<String, FieldRelationMember> entry : relationFieldMap.entrySet()) {
            // 关系属性信息
            FieldRelationMember fieldRelationMember = entry.getValue();

            // 获取关系的预处理
            GenerateSqlPartFunctionalInterface<?, ?> generateSqlPart = record.getRelationBuilderMap()
                .get(fieldRelationMember.getName());
            RelationshipRecordWithFunctionalInterface relationshipRecordWith = record.getRelationRecordMap()
                .get(fieldRelationMember.getName());

            if (generateSqlPart == null || relationshipRecordWith == null || !attachedRelationship) {
                continue;
            }

            // 关联关系字段处理
            RelationSubQuery relationSubQuery = fieldRelationMember.getRelationSubQuery();

            // Builder数组 [0 -> 中间表操作 , 1 -> 目标表操作]
            Builder<?, ?>[] builderArr = relationSubQuery.prepareBuilderArr(originalMetadataMapList, generateSqlPart);

            // 中间表数据
            RecordList<?, ?> relationRecords = getRelationRecordsInCache(cacheRecords, builderArr[0],
                () -> relationSubQuery.dealBatchForRelation(builderArr[0]));

            // 本级关系查询
            RecordList<?, ?> targetRecordList = getTargetRecordsInCache(cacheRecords, builderArr,
                relationshipRecordWith, () -> relationSubQuery.dealBatchForTarget(builderArr[1], relationRecords));

            // 递归处理下级关系, 并筛选当前 record 所需要的属性
            List<?> objects = relationSubQuery.filterBatchRecord(record, targetRecordList, cacheRecords);

            // 是否是集合
            if (fieldRelationMember.isPlural()) {
                // 关系属性赋值
                fieldRelationMember.fieldSet(entity, objects);
            } else {
                // 关系属性赋值
                fieldRelationMember.fieldSet(entity, objects.size() == 0 ? null : objects.get(0));
            }
        }
    }

    /**
     * 在内存缓存中优先查找目标值
     * @param cacheRecords 缓存map
     * @param builder 关系表查询构造器
     * @param closure 真实业务逻辑实现
     * @return 批量结果集
     */
    protected static RecordList<?, ?> getRelationRecordsInCache(Map<String, RecordList<?, ?>> cacheRecords,
        @Nullable Builder<?, ?> builder, GenerateRecordListFunctionalInterface closure) {
        // 有缓存有直接返回, 没有就执行后返回
        // 因为没有更新操作, 所以直接返回原对象
        // new Builder<?, ?>[]{null, builder} 很关键
        return getRecordsInCache(cacheRecords, new Builder<?, ?>[]{null, builder}, closure);
    }

    /**
     * 在内存缓存中优先查找目标值
     * @param cacheRecords 缓存map
     * @param builderArr 查询构造器数组 [0 -> 中间表操作 , 1 -> 目标表操作]
     * @param relationshipRecordWith record 实现
     * @param closure 真实业务逻辑实现
     * @return 批量结果集
     */
    protected static RecordList<?, ?> getTargetRecordsInCache(Map<String, RecordList<?, ?>> cacheRecords,
        Builder<?, ?>[] builderArr, RelationshipRecordWithFunctionalInterface relationshipRecordWith,
        GenerateRecordListFunctionalInterface closure) {
        // 有缓存有直接返回, 没有就执行后返回
        RecordList<?, ?> recordList = getRecordsInCache(cacheRecords, builderArr, closure);
        // 使用复制结果
        RecordList<?, ?> recordsCopy = RecordFactory.copyRecordList(recordList);
        // 赋值关联关系
        for (Record<?, ?> theRecord : recordsCopy) {
            relationshipRecordWith.execute(theRecord);
        }
        return recordsCopy;
    }

    /**
     * 在内存缓存中优先查找目标值
     * @param cacheRecords 缓存map
     * @param builderArr 查询构造器数组 [0 -> 中间表操作 , 1 -> 目标表操作]
     * @param closure 真实业务逻辑实现
     * @return 批量结果集
     */
    protected static RecordList<?, ?> getRecordsInCache(Map<String, RecordList<?, ?>> cacheRecords,
        Builder<?, ?>[] builderArr, GenerateRecordListFunctionalInterface closure) {
        // 缓存keyName
        StringBuilder cacheKey = new StringBuilder();
        for (Builder<?, ?> builder : builderArr) {
            cacheKey.append(builder == null ? "" : builder.toSql(SqlType.SELECT));
            cacheKey.append('|');
        }

        // 有缓存有直接返回, 没有就执行后返回
        return cacheRecords.computeIfAbsent(cacheKey.toString(), theKey -> closure.execute());
    }

    /**
     * Model 信息
     * @return ModelShadow
     */
    protected ModelShadowProvider getModelShadow() {
        return container.getBean(ModelShadowProvider.class);
    }

}
