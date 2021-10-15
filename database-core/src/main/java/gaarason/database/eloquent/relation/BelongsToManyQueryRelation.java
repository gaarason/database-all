package gaarason.database.eloquent.relation;

import gaarason.database.contract.eloquent.Model;
import gaarason.database.contract.eloquent.Record;
import gaarason.database.contract.eloquent.RecordList;
import gaarason.database.contract.function.GenerateSqlPartFunctionalInterface;
import gaarason.database.eloquent.annotation.BelongsToMany;
import gaarason.database.eloquent.appointment.SqlType;
import gaarason.database.provider.ModelShadowProvider;
import gaarason.database.support.Column;
import gaarason.database.support.RecordFactory;
import gaarason.database.util.ObjectUtils;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.*;

/**
 * 关联关系 多对多
 * @author xt
 */
public class BelongsToManyQueryRelation extends BaseRelationSubQuery {

    private final BelongsToManyTemplate belongsToManyTemplate;

    public BelongsToManyQueryRelation(Field field) {
        belongsToManyTemplate = new BelongsToManyTemplate(field);
    }

    @Override
    public String[] prepareSqlArr(List<Map<String, Column>> stringColumnMapList,
        GenerateSqlPartFunctionalInterface<?, ?> generateSqlPart) {

        return new String[]{generateSqlPart.execute(ObjectUtils.typeCast(belongsToManyTemplate.targetModel.newQuery())).toSql(
            SqlType.SUB_QUERY), belongsToManyTemplate.relationModel.newQuery()
            .whereIn(belongsToManyTemplate.foreignKeyForLocalModel,
                getColumnInMapList(stringColumnMapList, belongsToManyTemplate.localModelLocalKey))
            .toSql(SqlType.SELECT)};
    }

    @Override
    public RecordList<? extends Serializable, ? extends Serializable> dealBatchPrepare(String sql1) {
        return belongsToManyTemplate.relationModel.newQuery()
            .queryList(sql1, new ArrayList<>());
    }

    @Override
    public RecordList<?, ?> dealBatch(String sql0, RecordList<?, ?> relationRecordList) {
        List<Map<String, Object>> relationMaps = relationRecordList.toMapList();

        // 将中间表结果中的目标表外键 ,转化为可以使用 where in 查询的 set
        Set<Object> targetModelForeignKeySet = new HashSet<>();
        for (Map<String, Object> map : relationMaps) {
            Object result = map.get(belongsToManyTemplate.foreignKeyForTargetModel);
            if (null == result) {
                continue;
            }
            targetModelForeignKeySet.add(result);
        }

        if (targetModelForeignKeySet.isEmpty()) {
            return RecordFactory.newRecordList();
        }

        // 目标表结果
        RecordList<?, ?> targetRecordList = belongsToManyTemplate.targetModel.newQuery().whereRaw(sql0)
            .whereIn(belongsToManyTemplate.targetModelLocalKey, targetModelForeignKeySet)
            .get();

        // 循环关系表, 筛选本表需要的数据
        for (Record<?, ?> targetRecord : targetRecordList) {
            Object targetKey = targetRecord.getMetadataMap()
                .get(belongsToManyTemplate.targetModelLocalKey)
                .getValue();

            for (Map<String, Object> relationMap : relationMaps) {
                Object localModelKeyInMap = relationMap.get(belongsToManyTemplate.foreignKeyForLocalModel);
                Object targetModelKeyInMap = relationMap.get(belongsToManyTemplate.foreignKeyForTargetModel);

                if (targetModelKeyInMap.equals(targetKey)) {
                    // 暂存
                    // 存储到 RecordList 上
                    Set<Object> relationIds = targetRecordList.getCacheMap().computeIfAbsent(localModelKeyInMap,
                        key -> new HashSet<>());
                    relationIds.add(targetModelKeyInMap);
                }
            }
        }
        return targetRecordList;
    }

    @Override
    public List<? extends Serializable> filterBatchRecord(Record<?, ?> theRecord, RecordList<?, ?> targetRecordList,
        Map<String, RecordList<?, ?>> cacheRelationRecordList) {
        // 目标关系表的外键字段名
        String targetModelLocalKey = belongsToManyTemplate.targetModelLocalKey;
        // 本表的关系键值
        Object localModelLocalKeyValue = theRecord.getMetadataMap().get(belongsToManyTemplate.localModelLocalKey).getValue();

        // 本表应该关联的 目标表id列表
        Set<Object> targetModelLocalKayValueSet = targetRecordList.getCacheMap().get(localModelLocalKeyValue);

        List<Serializable> objectList = new ArrayList<>();
        List<? extends Serializable> objects = targetRecordList.toObjectList(cacheRelationRecordList);

        if (!objects.isEmpty()) {
            // 模型信息
            ModelShadowProvider.ModelInfo<?, ?> modelInfo = ModelShadowProvider.get(
                targetRecordList.get(0).getModel());
            // 字段信息
            ModelShadowProvider.FieldInfo fieldInfo = modelInfo.getColumnFieldMap().get(targetModelLocalKey);

            for (Serializable obj : objects) {
                // 目标值
                Object targetModelLocalKeyValue = ModelShadowProvider.fieldGet(fieldInfo, obj);
                // 满足则加入
                if (targetModelLocalKayValueSet != null && targetModelLocalKayValueSet.contains(
                    targetModelLocalKeyValue)) {
                    objectList.add(obj);
                }
            }
        }
        return objectList;
    }

    @Override
    public int attach(Record<?, ?> theRecord, RecordList<?, ?> targetRecords, Map<String, Object> relationDataMap) {
        if (targetRecords.isEmpty()) {
            return 0;
        }

        // 目标表的关系键(默认目标表的主键)
        Collection<Object> compatibleForeignKeyForTargetModelValues = targetRecords.toList(recordTemp ->
            recordTemp.getMetadataMap().get(belongsToManyTemplate.targetModelLocalKey).getValue());

        // 事物
        return belongsToManyTemplate.relationModel.newQuery().transaction(
            () -> attachWithTargetModelLocalKeyValues(theRecord, compatibleForeignKeyForTargetModelValues, relationDataMap, true)
        );
    }

    @Override
    public int attach(Record<?, ?> theRecord, Collection<Object> targetPrimaryKeyValues,
        Map<String, Object> relationDataMap) {
        if (targetPrimaryKeyValues.isEmpty()) {
            return 0;
        }

        // 事物
        return belongsToManyTemplate.relationModel.newQuery().transaction(() -> {
            // 目标表中的关系键的集合, 即使中间表中指向目标表的外键的集合
            Collection<Object> targetModelLocalKeyValues = targetModelLocalKeyValuesByPrimaryKeyValues(
                targetPrimaryKeyValues);

            return attachWithTargetModelLocalKeyValues(theRecord, targetModelLocalKeyValues, relationDataMap, true);
        });
    }

    @Override
    public int detach(Record<?, ?> theRecord) {
        // 本表的关系键值
        Object localModelLocalKeyValue = theRecord.getMetadataMap().get(belongsToManyTemplate.localModelLocalKey).getValue();

        return belongsToManyTemplate.relationModel.newQuery()
            .where(belongsToManyTemplate.foreignKeyForLocalModel, localModelLocalKeyValue)
            .delete();
    }

    @Override
    public int detach(Record<?, ?> theRecord, RecordList<?, ?> targetRecords) {
        // 目标表的关系键值列表
        List<Object> targetModelLocalKeyValues = targetRecords.toList(
            recordTemp -> recordTemp.getMetadataMap().get(belongsToManyTemplate.targetModelLocalKey).getValue());
        return detachWithTargetModelLocalKeyValues(theRecord, targetModelLocalKeyValues);
    }

    @Override
    public int detach(Record<?, ?> theRecord, Collection<Object> targetPrimaryKeyValues) {
        // 无需处理则直接返回
        if (targetPrimaryKeyValues.isEmpty()) {
            return 0;
        }
        // 事物
        return belongsToManyTemplate.relationModel.newQuery().transaction(() -> {
            // 目标表中的关系键的集合, 即使中间表中指向目标表的外键的集合
            Collection<Object> targetModelLocalKeyValues = targetModelLocalKeyValuesByPrimaryKeyValues(
                targetPrimaryKeyValues);
            return detachWithTargetModelLocalKeyValues(theRecord, targetModelLocalKeyValues);

        });

    }

    @Override
    public int sync(Record<?, ?> theRecord, RecordList<?, ?> targetRecords, Map<String, Object> relationDataMap) {
        // 目标表的关系键值列表
        List<Object> targetModelLocalKeyValues = targetRecords.toList(
            recordTemp -> recordTemp.getMetadataMap().get(belongsToManyTemplate.targetModelLocalKey).getValue());
        // 事物
        return belongsToManyTemplate.relationModel.newQuery().transaction(
            () -> syncWithTargetModelLocalKeyValues(theRecord, targetModelLocalKeyValues, relationDataMap));
    }

    @Override
    public int sync(Record<?, ?> theRecord, Collection<Object> targetPrimaryKeyValues, Map<String, Object> relationDataMap) {
        // 事物
        return belongsToManyTemplate.relationModel.newQuery().transaction(() -> {
            // 目标表中的关系键的集合, 即使中间表中指向目标表的外键的集合
            Collection<Object> targetModelLocalKeyValues = targetModelLocalKeyValuesByPrimaryKeyValues(
                targetPrimaryKeyValues);
            return syncWithTargetModelLocalKeyValues(theRecord, targetModelLocalKeyValues, relationDataMap);
        });
    }

    @Override
    public int toggle(Record<?, ?> theRecord, RecordList<?, ?> targetRecords, Map<String, Object> relationDataMap) {
        if (targetRecords.isEmpty()) {
            return 0;
        }

        // 目标表的关系键值的集合
        List<Object> targetModelLocalKeyValues = targetRecords.toList(
            recordTemp -> recordTemp.getMetadataMap().get(belongsToManyTemplate.targetModelLocalKey).getValue());
        // 事物
        return belongsToManyTemplate.relationModel.newQuery().transaction(
            () -> toggleWithTargetModelLocalKeyValues(theRecord, targetModelLocalKeyValues, relationDataMap));
    }

    @Override
    public int toggle(Record<?, ?> theRecord, Collection<Object> targetPrimaryKeyValues, Map<String, Object> relationDataMap) {
        if (targetPrimaryKeyValues.isEmpty()) {
            return 0;
        }
        // 事物
        return belongsToManyTemplate.relationModel.newQuery().transaction(() -> {
            // 目标表中的关系键值的集合, 即使中间表中指向目标表的外键的集合
            Collection<Object> targetModelLocalKeyValues = targetModelLocalKeyValuesByPrimaryKeyValues(targetPrimaryKeyValues);

            // 切换关系, 如果指定关系已存在，则解除，如果指定关系不存在，则增加
            return toggleWithTargetModelLocalKeyValues(theRecord, targetModelLocalKeyValues, relationDataMap);
        });
    }

    /**
     * 通过目标表的主键获取目标表的关系键
     * @param targetPrimaryKeyValues 目标表的主键集合
     * @return 目标表的关系键集合
     */
    protected Collection<Object> targetModelLocalKeyValuesByPrimaryKeyValues(
        Collection<Object> targetPrimaryKeyValues) {
        // 目标表中的关系键的集合, 即使中间表中指向目标表的外键的集合
        Collection<Object> targetModelLocalKeyValues;

        // 如果目标表的主键既是关系键, 那么可以简化
        if (belongsToManyTemplate.targetModelLocalKey.equals(
            belongsToManyTemplate.targetModel.getPrimaryKeyColumnName())) {
            // 目标表的主键 处理下 AbstractList
            targetModelLocalKeyValues = compatibleCollection(targetPrimaryKeyValues, belongsToManyTemplate.targetModel);

        } else {
            // 目标表中的关系键的集合, 即使中间表中指向目标表的外键的集合
            targetModelLocalKeyValues = belongsToManyTemplate.targetModel.newQuery()
                .select(belongsToManyTemplate.targetModelLocalKey)
                .whereIn(belongsToManyTemplate.targetModel.getPrimaryKeyColumnName(), targetPrimaryKeyValues)
                .get().toOneColumnList();
        }
        return targetModelLocalKeyValues;
    }

    /**
     * 更加关联关系
     * @param theRecord                 当前record
     * @param targetModelLocalKeyValues 目标表关系键集合 (已经经过兼容性处理了的)
     * @param relationDataMap           中间表新增是要附带的数据
     * @param checkAlreadyExist         是否多一次查询, 以验证关系是否已存在
     * @return 受影响的行数
     */
    protected int attachWithTargetModelLocalKeyValues(Record<?, ?> theRecord, Collection<Object> targetModelLocalKeyValues,
        Map<String, Object> relationDataMap, boolean checkAlreadyExist) {
        // 无需处理则直接返回
        if (targetModelLocalKeyValues.isEmpty()) {
            return 0;
        }

        // 本表的关系键值
        Object localModelLocalKeyValue = theRecord.getMetadataMap().get(belongsToManyTemplate.localModelLocalKey).getValue();

        if (checkAlreadyExist) {
            // 查询中间表(relationModel)是否存在已经存在对应的关系
            List<Object> alreadyExistTargetModelLocalKeyValueList = belongsToManyTemplate.relationModel.newQuery()
                .select(belongsToManyTemplate.foreignKeyForLocalModel,
                    belongsToManyTemplate.foreignKeyForTargetModel)
                .where(belongsToManyTemplate.foreignKeyForLocalModel, localModelLocalKeyValue)
                .whereIn(belongsToManyTemplate.foreignKeyForTargetModel, targetModelLocalKeyValues)
                .get().toList(recordTemp -> recordTemp.getMetadataMap().get(belongsToManyTemplate.foreignKeyForTargetModel).getValue());

            // 剔除已经存在的关系, 保留需要插入的ids
            targetModelLocalKeyValues.removeAll(alreadyExistTargetModelLocalKeyValueList);
        }

        // 无需处理则直接返回
        if (targetModelLocalKeyValues.isEmpty()) {
            return 0;
        }

        // 格式化, 并附带需要存储到中间表的信息
        List<String> columnList = new ArrayList<>();
        columnList.add(belongsToManyTemplate.foreignKeyForLocalModel);
        columnList.add(belongsToManyTemplate.foreignKeyForTargetModel);

        // 预处理中间表信息
        List<Object> middleValueList = new ArrayList<>();
        for (Map.Entry<String, Object> entry : relationDataMap.entrySet()) {
            columnList.add(entry.getKey());
            middleValueList.add(entry.getValue());
        }

        List<List<Object>> valuesList = new ArrayList<>();
        for (Object o : targetModelLocalKeyValues) {
            List<Object> valueList = new ArrayList<>();
            valueList.add(localModelLocalKeyValue);
            valueList.add(o);
            // 加入中间表数据
            valueList.addAll(middleValueList);
            valuesList.add(valueList);
        }

        // 插入
        return belongsToManyTemplate.relationModel.newQuery().select(columnList).valueList(valuesList).insert();
    }

    /**
     * 解除关联关系
     * @param theRecord                 当前record
     * @param targetModelLocalKeyValues 目标表关系键集合 (已经经过兼容性处理了的)
     * @return 受影响的行数
     */
    protected int detachWithTargetModelLocalKeyValues(Record<?, ?> theRecord, Collection<Object> targetModelLocalKeyValues) {
        // 本表的关系键值
        Object localModelLocalKeyValue = theRecord.getMetadataMap().get(belongsToManyTemplate.localModelLocalKey).getValue();

        return belongsToManyTemplate.relationModel.newQuery()
            .where(belongsToManyTemplate.foreignKeyForLocalModel, localModelLocalKeyValue)
            .whereIn(belongsToManyTemplate.foreignKeyForTargetModel, targetModelLocalKeyValues)
            .delete();
    }

    /**
     * 同步到关联关系, 任何不在指定范围的对应记录将会移除
     * @param theRecord                 当前record
     * @param targetModelLocalKeyValues 目标表关系键集合 (已经经过兼容性处理了的)
     * @param relationDataMap           中间表新增是要附带的数据
     * @return 受影响的行数
     */
    protected int syncWithTargetModelLocalKeyValues(Record<?, ?> theRecord, Collection<Object> targetModelLocalKeyValues,
        Map<String, Object> relationDataMap) {
        // 本表的关系键值
        Object localModelLocalKeyValue = theRecord.getMetadataMap().get(belongsToManyTemplate.localModelLocalKey).getValue();


        // 现存的关联关系, 不需要据需存在的, 解除
        int detachNum = belongsToManyTemplate.relationModel.newQuery()
            .where(belongsToManyTemplate.foreignKeyForLocalModel, localModelLocalKeyValue)
            .whereNotIn(belongsToManyTemplate.foreignKeyForTargetModel, targetModelLocalKeyValues)
            .delete();

        // 执行更新
        int attachNum = attachWithTargetModelLocalKeyValues(theRecord, targetModelLocalKeyValues, relationDataMap, true);

        return attachNum + detachNum;
    }

    /**
     * 切换关系, 如果指定关系已存在，则解除，如果指定关系不存在，则增加
     * @param theRecord                 当前record
     * @param targetModelLocalKeyValues 目标表关系键集合 (已经经过兼容性处理了的)
     * @param relationDataMap           中间表新增是要附带的数据
     * @return 受影响的行数
     */
    protected int toggleWithTargetModelLocalKeyValues(Record<?, ?> theRecord, Collection<Object> targetModelLocalKeyValues,
        Map<String, Object> relationDataMap) {
        if (targetModelLocalKeyValues.isEmpty()) {
            return 0;
        }
        // 本表的关系键值
        Object localModelLocalKeyValue = theRecord.getMetadataMap().get(belongsToManyTemplate.localModelLocalKey).getValue();

        // 现存的关联关系 中间表指向目标表的外键值的集合
        List<Object> alreadyExistTargetModelLocalKeyValues = belongsToManyTemplate.relationModel.newQuery()
            .select(belongsToManyTemplate.foreignKeyForTargetModel)
            .where(belongsToManyTemplate.foreignKeyForLocalModel, localModelLocalKeyValue)
            .whereIn(belongsToManyTemplate.foreignKeyForTargetModel, targetModelLocalKeyValues)
            .get().toOneColumnList();

        // 现存的关联关系, 解除
        int detachNum = !targetModelLocalKeyValues.isEmpty() ? belongsToManyTemplate.relationModel.newQuery()
            .where(belongsToManyTemplate.foreignKeyForLocalModel, localModelLocalKeyValue)
            .whereIn(belongsToManyTemplate.foreignKeyForTargetModel, targetModelLocalKeyValues)
            .delete() : 0;

        // 需要增加的关系(不存在就增加) 中间表指向目标表的外键值的集合
        Collection<Object> compatibleTargetModelLocalKeyValues = compatibleCollection(targetModelLocalKeyValues, belongsToManyTemplate.targetModel);
        compatibleTargetModelLocalKeyValues.removeAll(alreadyExistTargetModelLocalKeyValues);

        // 不存在的关系, 新增
        int attachNum = attachWithTargetModelLocalKeyValues(theRecord, compatibleTargetModelLocalKeyValues, relationDataMap, false);

        return attachNum + detachNum;
    }

    static class BelongsToManyTemplate {

        final Model<? extends Serializable, ? extends Serializable> relationModel; // user_teacher

        final String foreignKeyForLocalModel;// user_id

        final String localModelLocalKey; // user.id

        final Model<?, ?> targetModel; // teacher

        final String foreignKeyForTargetModel; // teacher_id

        final String targetModelLocalKey;  // teacher.id

        BelongsToManyTemplate(Field field) {
            BelongsToMany belongsToMany = field.getAnnotation(BelongsToMany.class);
            relationModel = getModelInstance(belongsToMany.relationModel()); // user_teacher
            foreignKeyForLocalModel = belongsToMany.foreignKeyForLocalModel(); // user_id
            localModelLocalKey = belongsToMany.localModelLocalKey(); // user.id
            targetModel = getModelInstance(field); // teacher
            foreignKeyForTargetModel = belongsToMany.foreignKeyForTargetModel(); // teacher_id
            targetModelLocalKey = belongsToMany.targetModelLocalKey();  // teacher.id
        }
    }
}
