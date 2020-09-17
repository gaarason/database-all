package gaarason.database.support;

import gaarason.database.contract.eloquent.Record;
import gaarason.database.contract.eloquent.RecordList;
import gaarason.database.contract.eloquent.relation.RelationSubQuery;
import gaarason.database.contract.function.GenerateRecordListFunctionalInterface;
import gaarason.database.contract.function.GenerateSqlPartFunctionalInterface;
import gaarason.database.contract.function.RelationshipRecordWithFunctionalInterface;
import gaarason.database.exception.EntityNewInstanceException;
import gaarason.database.provider.ModelShadowProvider;

import java.util.*;

public class RelationGetSupport<T, K> {

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
     * @param record               结果集
     * @param attachedRelationship 是否启用关联关系
     */
    public RelationGetSupport(Record<T, K> record, boolean attachedRelationship) {
        List<Record<T, K>> records = new ArrayList<>();
        records.add(record);
        this.attachedRelationship = attachedRelationship;
        this.records = RecordFactory.newRecordList(records);
    }

    public RelationGetSupport(List<Record<T, K>> records, boolean attachedRelationship) {
        this.attachedRelationship = attachedRelationship;
        this.records = RecordFactory.newRecordList(records);
    }

    public RelationGetSupport(RecordList<T, K> records, boolean attachedRelationship) {
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
     * @return 对象列表
     */
    public List<T> toObjectList(Map<String, RecordList<?, ?>> cacheRelationRecordList) {
        // 同级数据源
        List<Map<String, Column>> originalMetadataMapList = records.getOriginalMetadataMapList();

        List<T> list = new ArrayList<>();
        // 关联关系的临时性缓存
        for (Record<T, K> record : records) {
            // 模型信息
            ModelShadowProvider.ModelInfo<T, K> modelInfo = ModelShadowProvider.get(records.get(0).getModel());
            try {
                // 实体类的对象
                T entity = modelInfo.getEntityClass().newInstance();
                // 普通属性集合
                Map<String, ModelShadowProvider.FieldInfo> javaFieldMap = modelInfo.getJavaFieldMap();
                for (String fieldName : javaFieldMap.keySet()) {
                    // 普通属性信息
                    ModelShadowProvider.FieldInfo fieldInfo = javaFieldMap.get(fieldName);
                    // 普通属性赋值
                    ModelShadowProvider.fieldAssignment(fieldInfo, record.getMetadataMap(), entity, record);
                }

                // 关系属性集合
                Map<String, ModelShadowProvider.RelationFieldInfo> relationFieldMap = modelInfo.getRelationFieldMap();
                for (String fieldName : relationFieldMap.keySet()) {
                    // 关系属性信息
                    ModelShadowProvider.RelationFieldInfo relationFieldInfo = relationFieldMap.get(fieldName);

                    // 获取关系的预处理
                    GenerateSqlPartFunctionalInterface generateSqlPart = record.getRelationBuilderMap()
                        .get(relationFieldInfo.getName());
                    RelationshipRecordWithFunctionalInterface relationshipRecordWith = record.getRelationRecordMap()
                        .get(relationFieldInfo.getName());

                    if (generateSqlPart == null || relationshipRecordWith == null || !attachedRelationship) {
                        continue;
                    }

                    // 关联关系字段处理
                    RelationSubQuery relationSubQuery = relationFieldInfo.getRelationSubQuery();

                    // sql数组
                    String[] sqlArr = relationSubQuery.dealBatchSql(originalMetadataMapList, generateSqlPart);

                    // 本级关系查询
                    RecordList<?, ?> relationshipRecordList = getRecordListInCache(cacheRelationRecordList,
                        relationFieldInfo.getName(), record.getModel().getTableName(),
                        () -> relationSubQuery.dealBatch(sqlArr),
                        relationshipRecordWith, sqlArr);

                    // 递归处理下级关系, 并筛选当前 record 所需要的属性
                    List<?> objects = relationSubQuery.filterBatchRecord(record, relationshipRecordList,
                        cacheRelationRecordList);

                    // 是否是集合
                    if (relationFieldInfo.isCollection()) {
                        // 关系属性赋值
                        relationFieldInfo.getField().set(entity, objects);
                    } else {
                        // 关系属性赋值
                        relationFieldInfo.getField().set(entity, objects.size() == 0 ? null : objects.get(0));
                    }
                }
                list.add(entity);
            } catch (InstantiationException | IllegalAccessException e) {
                throw new EntityNewInstanceException(e.getMessage(), e);
            }

        }
        return list;
    }

    /**
     * 在内存缓存中优先查找目标值
     * @param cacheRelationRecordList 缓存map
     * @param key                     目标值
     * @param tableName               表名
     * @param closure                 真实业务逻辑实现
     * @param relationshipRecordWith  record 实现
     * @param sqlArr                  sql数组
     * @return 批量结果集
     */
    protected RecordList<?, ?> getRecordListInCache(Map<String, RecordList<?, ?>> cacheRelationRecordList, String key,
                                                    String tableName, GenerateRecordListFunctionalInterface closure,
                                                    RelationshipRecordWithFunctionalInterface relationshipRecordWith,
                                                    String[] sqlArr) {
        // 缓存keyName
        String cacheKeyName = key + "|" + Arrays.toString(sqlArr) + "|" + tableName;

        // 有缓存有直接返回, 没有就执行后返回
        RecordList<?, ?> recordList = cacheRelationRecordList.computeIfAbsent(cacheKeyName,
            theKey -> closure.execute());

        // 使用复制结果
        RecordList<?, ?> recordsCopy = RecordFactory.copyRecordList(recordList);

        // 赋值关联关系
        for (Record<?, ?> record : recordsCopy) {
            relationshipRecordWith.execute(record);
        }
        return recordsCopy;
    }

}
