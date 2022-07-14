package gaarason.database.support;

import gaarason.database.appointment.Column;
import gaarason.database.contract.eloquent.Record;
import gaarason.database.contract.eloquent.RecordList;
import gaarason.database.contract.eloquent.relation.RelationSubQuery;
import gaarason.database.contract.function.GenerateRecordListFunctionalInterface;
import gaarason.database.contract.function.GenerateSqlPartFunctionalInterface;
import gaarason.database.contract.function.RelationshipRecordWithFunctionalInterface;
import gaarason.database.core.Container;
import gaarason.database.provider.ModelShadowProvider;

import java.io.Serializable;
import java.util.*;

/**
 * 关联关系获取
 * @author xt
 */
public class RelationGetSupport<T extends Serializable, K extends Serializable> extends Container.SimpleKeeper {

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
        List<Map<String, Column>> originalMetadataMapList = records.getOriginalMetadataMapList();
        ModelShadowProvider modelShadow = getModelShadow();

        List<T> list = new ArrayList<>();
        // 关联关系的临时性缓存
        for (Record<T, K> theRecord : records) {
            // 模型信息
            ModelMember<T, K> modelMember = modelShadow.get(records.get(0).getModel());
            EntityMember<T> entityMember = modelMember.getEntityMember();

            // 实体类的对象
            T entity = entityMember.newInstance();
            // 普通属性集合
            modelShadow.entityAssignment(entity, theRecord);

            // 关系属性集合
            Map<String, FieldRelationMember> relationFieldMap = entityMember.getRelationFieldMap();
            for (Map.Entry<String, FieldRelationMember> entry : relationFieldMap.entrySet()) {
                // 关系属性信息
                FieldRelationMember fieldRelationMember = entry.getValue();

                // 获取关系的预处理
                GenerateSqlPartFunctionalInterface<?, ?> generateSqlPart = theRecord.getRelationBuilderMap()
                    .get(fieldRelationMember.getName());
                RelationshipRecordWithFunctionalInterface relationshipRecordWith = theRecord.getRelationRecordMap()
                    .get(fieldRelationMember.getName());

                if (generateSqlPart == null || relationshipRecordWith == null || !attachedRelationship) {
                    continue;
                }

                // 关联关系字段处理
                RelationSubQuery relationSubQuery = fieldRelationMember.getRelationSubQuery();

                // sql数组
                String[] sqlArr = relationSubQuery.prepareSqlArr(originalMetadataMapList, generateSqlPart);

                // 中间表数据
                RecordList<?, ?> relationRecords = getRelationRecordsInCache(cacheRecords, sqlArr[1],
                    () -> relationSubQuery.dealBatchPrepare(sqlArr[1]));

                // 本级关系查询
                RecordList<?, ?> targetRecordList = getTargetRecordsInCache(cacheRecords, sqlArr,
                    relationshipRecordWith, () -> relationSubQuery.dealBatch(sqlArr[0], relationRecords));

                // 递归处理下级关系, 并筛选当前 record 所需要的属性
                List<?> objects = relationSubQuery.filterBatchRecord(theRecord, targetRecordList,
                    cacheRecords);

                // 是否是集合
                if (fieldRelationMember.isCollection()) {
                    // 关系属性赋值
                    fieldRelationMember.fieldSet(entity, objects);
                } else {
                    // 关系属性赋值
                    fieldRelationMember.fieldSet(entity, objects.size() == 0 ? null : objects.get(0));
                }
            }
            list.add(entity);
        }
        return list;
    }

    /**
     * 在内存缓存中优先查找目标值
     * @param cacheRecords 缓存map
     * @param sql sql
     * @param closure 真实业务逻辑实现
     * @return 批量结果集
     */
    protected RecordList<?, ?> getRelationRecordsInCache(Map<String, RecordList<?, ?>> cacheRecords,
        String sql, GenerateRecordListFunctionalInterface closure) {
        // 有缓存有直接返回, 没有就执行后返回
        // 因为没有更新操作, 所以直接返回原对象
        // new String[]{sql, ""} 很关键
        return getRecordsInCache(cacheRecords, new String[]{sql, ""}, closure);
    }


    /**
     * 在内存缓存中优先查找目标值
     * @param cacheRecords 缓存map
     * @param sqlArr sql数组
     * @param relationshipRecordWith record 实现
     * @param closure 真实业务逻辑实现
     * @return 批量结果集
     */
    protected RecordList<?, ?> getTargetRecordsInCache(Map<String, RecordList<?, ?>> cacheRecords,
        String[] sqlArr,
        RelationshipRecordWithFunctionalInterface relationshipRecordWith,
        GenerateRecordListFunctionalInterface closure) {
        // 有缓存有直接返回, 没有就执行后返回
        RecordList<?, ?> recordList = getRecordsInCache(cacheRecords, sqlArr, closure);
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
     * @param sqlArr sql数组
     * @param closure 真实业务逻辑实现
     * @return 批量结果集
     */
    protected RecordList<?, ?> getRecordsInCache(Map<String, RecordList<?, ?>> cacheRecords, String[] sqlArr,
        GenerateRecordListFunctionalInterface closure) {
        // 缓存keyName
        String cacheKeyName = Arrays.toString(sqlArr);
        // 有缓存有直接返回, 没有就执行后返回
        return cacheRecords.computeIfAbsent(cacheKeyName,
            theKey -> closure.execute());
    }

    /**
     * Model 信息
     * @return ModelShadow
     */
    protected ModelShadowProvider getModelShadow() {
        return container.getBean(ModelShadowProvider.class);
    }

}
