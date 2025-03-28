package gaarason.database.support;

import gaarason.database.appointment.RelationCache;
import gaarason.database.appointment.SqlType;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

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

    public T toObject(RelationCache cache) {
        return toObjectList(cache).get(0);
    }

    public List<T> toObjectList() {
        RelationCache cache = new RelationCache();
        return toObjectList(cache);
    }

    /**
     * 转化为对象列表
     * @param cache 结果集缓存(用于优化递归算法)
     * @return 对象列表s
     */
    public List<T> toObjectList(RelationCache cache) {
        // 同级数据源
        List<Map<String, Object>> originalMetadataMapList = records.getMetadata();
        ModelShadowProvider modelShadow = getModelShadow();
        List<T> list = new ArrayList<>();
        for (Record<T, K> theRecord : records) {
            /*
             * 生成对象
             * a. 单表属性的赋值
             * b. 关联关系属性的赋值
             * c. 关联关系操作属性的赋值
             */
            T entity = generateEntity(theRecord, cache, originalMetadataMapList, modelShadow);

            // 增加到结果中
            list.add(entity);
        }
        return list;
    }

    /**
     * 生成对象
     * @param record 查询结果集
     * @param cache 结果集缓存(用于优化递归算法)
     * @param metadata 同级数据源
     * @param modelShadow Model信息大全
     */
    protected T generateEntity(Record<T, K> record, RelationCache cache,
        List<Map<String, Object>> metadata, ModelShadowProvider modelShadow) {

        // 模型信息
        ModelMember<T, K> modelMember = modelShadow.get(records.get(0).getModel());
        EntityMember<T, K> entityMember = modelMember.getEntityMember();

        // 实体类的对象 (仅包含数据库字段属性)
        T entity = modelShadow.entityAssignment(entityMember.getEntityClass(), record);

        // 处理关联关系属性
        dealRelationField(entity, record, entityMember, cache, metadata);

        return entity;
    }

    /**
     * 处理entity的关联关系属性
     * @param entity 实体对象
     * @param record 查询结果集
     * @param entityMember 数据库实体信息
     * @param cache 结果集缓存(用于优化递归算法)
     * @param metadata 同级数据源
     */
    private void dealRelationField(T entity, Record<T, K> record, EntityMember<T, K> entityMember,
            RelationCache cache, List<Map<String, Object>> metadata) {
        if (!attachedRelationship) {
            return;
        }

        // 当前层级 +1
        int level = cache.level.incrementAndGet();

        // 关联关系 补充设置
        Map<String, Record.Relation> relationMap = record.getRelationMap();

        // 已经声明使用的关联关系属性集合
        for (Map.Entry<String, Record.Relation> relationEntry : relationMap.entrySet()) {
            // 目标属性名
            String targetFieldName = relationEntry.getKey();
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
            Builder<?, ?, ?> relationBuilder = relationSubQuery.prepareRelationBuilder(metadata);

            // 中间表数据
            RecordList<?, ?> relationRecords = getRelationRecordsInCache(cache.cacheRelationRecordList, relationBuilder,
                () -> relationSubQuery.dealBatchForRelation(relationBuilder));

            // 关联关系统计查询
            if (relationOperation) {
                // 目标表，查询构造器
                Builder<?, ?, ?> targetBuilder = relationSubQuery.prepareTargetBuilderByRelationOperation(metadata,
                    relationRecords, relation.operationBuilder, relation.customBuilder);

                // 本级关系查询
                RecordList<?, ?> targetRecordList = getTargetRecordsInCache(cache.cacheRelationRecordList, targetBuilder,
                    relation.recordWrapper,
                    () -> relationSubQuery.dealBatchForTargetByRelationOperation(targetBuilder, relationRecords));

                // 递归处理下级关系, 并筛选当前 record 所需要的属性
                Map<String, Object> map = relationSubQuery.filterBatchRecordByRelationOperation(record,
                    targetRecordList, cache);
                // 目标属性信息
                FieldMember<?> targetFieldMember = entityMember.getFieldMemberByFieldName(targetFieldName);
                // 目标属性赋值 - 统计属性 - 单数
                Object targetFieldValue = map.get(targetFieldName);
                // 类型转化
                Object targetFieldValueDeserialize = targetFieldMember.deserialize(targetFieldValue);
                // 赋值
                targetFieldMember.fieldSet(entity, targetFieldValueDeserialize);
            }
            // 关联关系查询
            else {
                // 目标表，查询构造器
                Builder<?, ?, ?> targetBuilder = relationSubQuery.prepareTargetBuilder(metadata, relationRecords,
                    relation.operationBuilder, relation.customBuilder);

                // 本级关系查询
                RecordList<?, ?> targetRecordList = getTargetRecordsInCache(cache.cacheRelationRecordList, targetBuilder,
                        relation.recordWrapper, () -> relationSubQuery.dealBatchForTarget(targetBuilder, relationRecords));

                // 当前对象类
                Class<T> entityClass = entityMember.getEntityClass();
                // 过滤结果集所需的必要元素组成的key
                String filterBatchRecordCacheKey = relationSubQuery.filterBatchRecordCacheKey(record, targetRecordList);
                // 缓存键
                String cacheKey = entityClass + "|" + targetFieldName + "@" + filterBatchRecordCacheKey + "@" + level;

                // 优先命中缓存
                List<?> objects = getRelationObjectsInCache(cache.cacheRelationObjectList, cacheKey,
                        () -> relationSubQuery.filterBatchRecord(record, targetRecordList, cache));

                // 深度拷贝
                objects = ObjectUtils.deepCopy(objects);

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
        // 当前层级 -1
        cache.level.decrementAndGet();
    }

    /**
     * 在内存缓存中优先查找目标值
     * @param cacheObjects 缓存map
     * @param cacheKey 缓存key
     * @param closure 真实业务逻辑实现
     * @return 批量结果对象集
     */
    protected static List<?> getRelationObjectsInCache(Map<String, List<?>> cacheObjects, String cacheKey, Supplier<List<?>> closure) {
        // 不可使用 map.computeIfAbsent, 因为需要递归调用
        List<?> result = cacheObjects.get(cacheKey);
        if (result == null) {
            result = closure.get();
            cacheObjects.put(cacheKey, result);
        }
        return result;
    }

    /**
     * 在内存缓存中优先查找目标值
     * @param cacheRecords 缓存map
     * @param builder 关系表查询构造器
     * @param closure 真实业务逻辑实现
     * @return 批量结果集
     */
    protected static RecordList<?, ?> getRelationRecordsInCache(Map<String, RecordList<?, ?>> cacheRecords,
        @Nullable Builder<?, ?, ?> builder, GenerateRecordListFunctionalInterface closure) {
        // 有缓存有直接返回, 没有就执行后返回
        // 因为没有更新操作, 所以直接返回原对象
        // new Builder<?, ?, ?>[]{null, builder} 很关键
        return getRecordsInCache(cacheRecords, builder, closure);
    }

    /**
     * 在内存缓存中优先查找目标值
     * @param cacheRecords 缓存map
     * @param builder 查询构造器 目标表操作
     * @param relationshipRecordWith record 实现
     * @param closure 真实业务逻辑实现
     * @return 批量结果集
     */
    protected static RecordList<?, ?> getTargetRecordsInCache(Map<String, RecordList<?, ?>> cacheRecords,
        @Nullable Builder<?, ?, ?> builder, RecordWrapper relationshipRecordWith,
        GenerateRecordListFunctionalInterface closure) {
        // 有缓存有直接返回, 没有就执行后返回
        // 此处降低查询次数
        RecordList<?, ?> recordList = getRecordsInCache(cacheRecords, builder, closure);
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
     * @param builder 查询构造器
     * @param closure 真实业务逻辑实现
     * @return 批量结果集
     */
    protected static RecordList<?, ?> getRecordsInCache(Map<String, RecordList<?, ?>> cacheRecords,
        @Nullable Builder<?, ?, ?> builder, GenerateRecordListFunctionalInterface closure) {
        // 缓存keyName
        String cacheKey = builder == null ? "" : builder.toSql(SqlType.SELECT, (sql, parameters) -> sql + parameters);

        // 有缓存有直接返回, 没有就执行后返回
        return cacheRecords.computeIfAbsent(cacheKey, theKey -> closure.execute());
    }

    /**
     * Model 信息
     * @return ModelShadow
     */
    protected ModelShadowProvider getModelShadow() {
        return container.getBean(ModelShadowProvider.class);
    }

}
