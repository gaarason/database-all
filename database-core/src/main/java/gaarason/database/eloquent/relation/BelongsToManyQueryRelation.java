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

import java.lang.reflect.Field;
import java.util.*;

public class BelongsToManyQueryRelation extends BaseRelationSubQuery {

    private final BelongsToManyTemplate belongsToManyTemplate;

    public BelongsToManyQueryRelation(Field field) {
        belongsToManyTemplate = new BelongsToManyTemplate(field);
    }

    @Override
    public String[] prepareSqlArr(List<Map<String, Column>> stringColumnMapList,
                                  GenerateSqlPartFunctionalInterface generateSqlPart) {
        return new String[]{generateSqlPart.execute(belongsToManyTemplate.targetModel.newQuery()).toSql(
            SqlType.SUB_QUERY), belongsToManyTemplate.relationModel.newQuery()
            .whereIn(belongsToManyTemplate.foreignKeyForLocalModel,
                getColumnInMapList(stringColumnMapList, belongsToManyTemplate.localModelLocalKey))
            .toSql(SqlType.SELECT)};
    }

    @Override
    public RecordList<?, ?> dealBatchPrepare(String sql1) {
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
            if (null == result)
                continue;
            targetModelForeignKeySet.add(result);
        }

        if (targetModelForeignKeySet.size() == 0) {
            return RecordFactory.newRecordList();
        }

        // 目标表结果
        RecordList<?, ?> targetRecordList = belongsToManyTemplate.targetModel.newQuery().whereRaw(sql0)
            .whereIn(belongsToManyTemplate.targetModelLocalKey, targetModelForeignKeySet)
            .get();

        // 循环关系表, 筛选本表需要的数据
        for (Record<?, ?> targetRecord : targetRecordList) {
            String targetKey = targetRecord.getMetadataMap()
                .get(belongsToManyTemplate.targetModelLocalKey)
                .getValue()
                .toString();

            for (Map<String, Object> relationMap : relationMaps) {
                String localModelKeyInMap  = relationMap.get(belongsToManyTemplate.foreignKeyForLocalModel).toString();
                String targetModelKeyInMap = relationMap.get(belongsToManyTemplate.foreignKeyForTargetModel).toString();

                if (targetModelKeyInMap.equals(targetKey)) {
                    // 暂存
                    // 存储到 RecordList 上
                    Set<String> relationIds = targetRecordList.getCacheMap().computeIfAbsent(localModelKeyInMap,
                        key -> new HashSet<>());
                    relationIds.add(targetModelKeyInMap);
                }
            }
        }
        return targetRecordList;
    }

    @Override
    public List<?> filterBatchRecord(Record<?, ?> record, RecordList<?, ?> TargetRecordList,
                                     Map<String, RecordList<?, ?>> cacheRelationRecordList) {
        // 目标关系表的外键字段名
        String targetModelLocalKey = belongsToManyTemplate.targetModelLocalKey;
        // 本表的关系键值
        String localModelLocalKeyValue = String.valueOf(
            record.getMetadataMap().get(belongsToManyTemplate.localModelLocalKey).getValue());

        // 本表应该关联的 目标表id列表
        Set<String> targetModelLocalKayValueSet = TargetRecordList.getCacheMap().get(localModelLocalKeyValue);

        List<Object> objectList = new ArrayList<>();
        List<?>      objects    = TargetRecordList.toObjectList(cacheRelationRecordList);

        if (objects.size() > 0) {
            // 模型信息
            ModelShadowProvider.ModelInfo<?, ?> modelInfo = ModelShadowProvider.get(
                TargetRecordList.get(0).getModel());
            // 字段信息
            ModelShadowProvider.FieldInfo fieldInfo = modelInfo.getColumnFieldMap().get(targetModelLocalKey);

            for (Object obj : objects) {
                // 目标值
                String targetModelLocalKeyValue = String.valueOf(ModelShadowProvider.fieldGet(fieldInfo, obj));
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
    public int attach(Record<?, ?> record, RecordList<?, ?> targetRecords, Map<String, String> stringStringMap) {
        if (targetRecords.size() == 0)
            return 0;

        // 目标表的关系键(默认目标表的主键)
        Collection<String> compatibleForeignKeyForTargetModelValues = targetRecords.toList(recordTemp -> String.valueOf(
            recordTemp.getMetadataMap().get(belongsToManyTemplate.targetModelLocalKey).getValue()));

        // 事物
        return belongsToManyTemplate.relationModel.newQuery().transaction(
            () -> attachWithTargetModelLocalKeyValues(record, compatibleForeignKeyForTargetModelValues, stringStringMap, true)
        );
    }

    @Override
    public int attach(Record<?, ?> record, Collection<String> targetPrimaryKeyValues,
                      Map<String, String> stringStringMap) {
        if (targetPrimaryKeyValues.size() == 0)
            return 0;

        // 事物
        return belongsToManyTemplate.relationModel.newQuery().transaction(() -> {
            // 目标表中的关系键的集合, 即使中间表中指向目标表的外键的集合
            Collection<String> targetModelLocalKeyValues = targetModelLocalKeyValuesByPrimaryKeyValues(
                targetPrimaryKeyValues);

            return attachWithTargetModelLocalKeyValues(record, targetModelLocalKeyValues, stringStringMap, true);
        });
    }

    @Override
    public int detach(Record<?, ?> record) {
        // 本表的关系键值
        String localModelLocalKeyValue = String.valueOf(
            record.getMetadataMap().get(belongsToManyTemplate.localModelLocalKey).getValue());

        return belongsToManyTemplate.relationModel.newQuery()
            .where(belongsToManyTemplate.foreignKeyForLocalModel, localModelLocalKeyValue)
            .delete();
    }

    @Override
    public int detach(Record<?, ?> record, RecordList<?, ?> targetRecords) {
        // 目标表的关系键值列表
        List<String> targetModelLocalKeyValues = targetRecords.toList(
            recordTemp -> String.valueOf(
                recordTemp.getMetadataMap().get(belongsToManyTemplate.targetModelLocalKey).getValue()));
        return detachWithTargetModelLocalKeyValues(record, targetModelLocalKeyValues);
    }

    @Override
    public int detach(Record<?, ?> record, Collection<String> targetPrimaryKeyValues) {
        // 无需处理则直接返回
        if (targetPrimaryKeyValues.isEmpty()) {
            return 0;
        }
        // 事物
        return belongsToManyTemplate.relationModel.newQuery().transaction(() -> {
            // 目标表中的关系键的集合, 即使中间表中指向目标表的外键的集合
            Collection<String> targetModelLocalKeyValues = targetModelLocalKeyValuesByPrimaryKeyValues(
                targetPrimaryKeyValues);
            return detachWithTargetModelLocalKeyValues(record, targetModelLocalKeyValues);

        });

    }

    @Override
    public int sync(Record<?, ?> record, RecordList<?, ?> targetRecords, Map<String, String> stringStringMap) {
        // 目标表的关系键值列表
        List<String> targetModelLocalKeyValues = targetRecords.toList(
            recordTemp -> String.valueOf(
                recordTemp.getMetadataMap().get(belongsToManyTemplate.targetModelLocalKey).getValue()));
        // 事物
        return belongsToManyTemplate.relationModel.newQuery().transaction(
            () -> syncWithTargetModelLocalKeyValues(record, targetModelLocalKeyValues, stringStringMap));
    }

    @Override
    public int sync(Record<?, ?> record, Collection<String> targetPrimaryKeyValues,
                    Map<String, String> stringStringMap) {
        // 事物
        return belongsToManyTemplate.relationModel.newQuery().transaction(() -> {
            // 目标表中的关系键的集合, 即使中间表中指向目标表的外键的集合
            Collection<String> targetModelLocalKeyValues = targetModelLocalKeyValuesByPrimaryKeyValues(
                targetPrimaryKeyValues);
            return syncWithTargetModelLocalKeyValues(record, targetModelLocalKeyValues, stringStringMap);
        });
    }

    @Override
    public int toggle(Record<?, ?> record, RecordList<?, ?> targetRecords, Map<String, String> stringStringMap) {
        if (targetRecords.isEmpty()) {
            return 0;
        }

        // 目标表的关系键值的集合
        List<String> targetModelLocalKeyValues = targetRecords.toList(
            recordTemp -> String.valueOf(
                recordTemp.getMetadataMap().get(belongsToManyTemplate.targetModelLocalKey).getValue()));
        // 事物
        return belongsToManyTemplate.relationModel.newQuery().transaction(
            () -> toggleWithTargetModelLocalKeyValues(record, targetModelLocalKeyValues, stringStringMap));
    }

    @Override
    public int toggle(Record<?, ?> record, Collection<String> targetPrimaryKeyValues, Map<String, String> stringStringMap) {
        if (targetPrimaryKeyValues.isEmpty()) {
            return 0;
        }
        // 事物
        return belongsToManyTemplate.relationModel.newQuery().transaction(() -> {
            // 目标表中的关系键值的集合, 即使中间表中指向目标表的外键的集合
            Collection<String> targetModelLocalKeyValues = targetModelLocalKeyValuesByPrimaryKeyValues(
                targetPrimaryKeyValues);

            return toggleWithTargetModelLocalKeyValues(record, targetModelLocalKeyValues, stringStringMap);
        });
    }

    /**
     * 通过目标表的主键获取目标表的关系键
     * @param targetPrimaryKeyValues 目标表的主键集合
     * @return 目标表的关系键集合
     */
    protected Collection<String> targetModelLocalKeyValuesByPrimaryKeyValues(
        Collection<String> targetPrimaryKeyValues) {
        // 目标表中的关系键的集合, 即使中间表中指向目标表的外键的集合
        Collection<String> targetModelLocalKeyValues;

        // 如果目标表的主键即是关系键, 那么可以简化
        if (belongsToManyTemplate.targetModelLocalKey.equals(
            belongsToManyTemplate.targetModel.getPrimaryKeyColumnName())) {
            // 目标表的主键 处理下 AbstractList
            targetModelLocalKeyValues = compatibleCollection(targetPrimaryKeyValues);

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
     * @param record                    当前record
     * @param targetModelLocalKeyValues 目标表关系键集合
     * @param stringStringMap           中间表新增是要附带的数据
     * @param checkAlreadyExist         是否多一次查询, 以验证关系是否已存在
     * @return 受影响的行数
     */
    protected int attachWithTargetModelLocalKeyValues(Record<?, ?> record, Collection<String> targetModelLocalKeyValues,
                                                      Map<String, String> stringStringMap, boolean checkAlreadyExist) {
        // 无需处理则直接返回
        if (targetModelLocalKeyValues.isEmpty()) {
            return 0;
        }

        // 本表的关系键值
        String localModelLocalKeyValue = String.valueOf(
            record.getMetadataMap().get(belongsToManyTemplate.localModelLocalKey).getValue());

        if (checkAlreadyExist) {
            // 查询中间表(relationModel)是否存在已经存在对应的关系
            List<String> AlreadyExistTargetModelLocalKeyValueList = belongsToManyTemplate.relationModel.newQuery()
                .select(belongsToManyTemplate.foreignKeyForLocalModel,
                    belongsToManyTemplate.foreignKeyForTargetModel)
                .where(belongsToManyTemplate.foreignKeyForLocalModel, localModelLocalKeyValue)
                .whereIn(belongsToManyTemplate.foreignKeyForTargetModel, targetModelLocalKeyValues)
                .get().toList(recordTemp -> String.valueOf(
                    recordTemp.getMetadataMap().get(belongsToManyTemplate.foreignKeyForTargetModel).getValue()));

            // 剔除已经存在的关系, 保留需要插入的ids
            targetModelLocalKeyValues.removeAll(AlreadyExistTargetModelLocalKeyValueList);
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
        List<String> middleValueList = new ArrayList<>();
        for (Map.Entry<String, String> entry : stringStringMap.entrySet()) {
            columnList.add(entry.getKey());
            middleValueList.add(entry.getValue());
        }

        List<List<String>> valuesList = new ArrayList<>();
        for (Object o : targetModelLocalKeyValues) {
            List<String> valueList = new ArrayList<>();
            valueList.add(localModelLocalKeyValue);
            valueList.add(o.toString());
            // 加入中间表数据
            valueList.addAll(middleValueList);
            valuesList.add(valueList);
        }

        // 插入
        return belongsToManyTemplate.relationModel.newQuery().select(columnList).valueList(valuesList).insert();
    }

    /**
     * 解除关联关系
     * @param record                    当前record
     * @param targetModelLocalKeyValues 目标表关系键集合
     * @return 受影响的行数
     */
    protected int detachWithTargetModelLocalKeyValues(Record<?, ?> record, Collection<String> targetModelLocalKeyValues) {
        // 本表的关系键值
        String localModelLocalKeyValue = String.valueOf(
            record.getMetadataMap().get(belongsToManyTemplate.localModelLocalKey).getValue());

        return belongsToManyTemplate.relationModel.newQuery()
            .where(belongsToManyTemplate.foreignKeyForLocalModel, localModelLocalKeyValue)
            .whereIn(belongsToManyTemplate.foreignKeyForTargetModel, targetModelLocalKeyValues)
            .delete();
    }

    /**
     * 同步到关联关系, 任何不在指定范围的对应记录将会移除
     * @param record                    当前record
     * @param targetModelLocalKeyValues 目标表关系键集合
     * @param stringStringMap           中间表新增是要附带的数据
     * @return 受影响的行数
     */
    protected int syncWithTargetModelLocalKeyValues(Record<?, ?> record, Collection<String> targetModelLocalKeyValues,
                                                    Map<String, String> stringStringMap) {
        // 本表的关系键值
        String localModelLocalKeyValue = String.valueOf(
            record.getMetadataMap().get(belongsToManyTemplate.localModelLocalKey).getValue());


        // 现存的关联关系, 不需要据需存在的, 解除
        int detachNum = belongsToManyTemplate.relationModel.newQuery()
            .where(belongsToManyTemplate.foreignKeyForLocalModel, localModelLocalKeyValue)
            .whereNotIn(belongsToManyTemplate.foreignKeyForTargetModel, targetModelLocalKeyValues)
            .delete();

        // 执行更新
        int attachNum = attachWithTargetModelLocalKeyValues(record, targetModelLocalKeyValues, stringStringMap, true);

        return attachNum + detachNum;
    }

    /**
     * 切换关系, 如果指定关系已存在，则解除，如果指定关系不存在，则增加
     * @param record                    当前record
     * @param targetModelLocalKeyValues 目标表关系键集合
     * @param stringStringMap           中间表新增是要附带的数据
     * @return 受影响的行数
     */
    protected int toggleWithTargetModelLocalKeyValues(Record<?, ?> record, Collection<String> targetModelLocalKeyValues,
                                                      Map<String, String> stringStringMap) {
        if(targetModelLocalKeyValues.isEmpty()){
            return 0;
        }
        // 本表的关系键值
        String localModelLocalKeyValue = String.valueOf(
            record.getMetadataMap().get(belongsToManyTemplate.localModelLocalKey).getValue());

        // 现存的关联关系 中间表指向目标表的外键值的集合
        List<String> alreadyExistTargetModelLocalKeyValues = belongsToManyTemplate.relationModel.newQuery()
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
        // compatibleTargetModelLocalKeyValues 与 targetModelLocalKeyValues 可能是同一个对象(相同内存地址)
        Collection<String> compatibleTargetModelLocalKeyValues = compatibleCollection(targetModelLocalKeyValues);
        compatibleTargetModelLocalKeyValues.removeAll(alreadyExistTargetModelLocalKeyValues);

        // 不存在的关系, 新增
        int attachNum = attachWithTargetModelLocalKeyValues(record, compatibleTargetModelLocalKeyValues, stringStringMap, false);

        return attachNum + detachNum;
    }

    static class BelongsToManyTemplate {

        final Model<?, ?> relationModel; // user_teacher

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
