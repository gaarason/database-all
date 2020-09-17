package gaarason.database.eloquent.relation;

import gaarason.database.contract.eloquent.Model;
import gaarason.database.contract.eloquent.Record;
import gaarason.database.contract.eloquent.RecordList;
import gaarason.database.contract.function.GenerateSqlPartFunctionalInterface;
import gaarason.database.eloquent.annotation.BelongsToMany;
import gaarason.database.eloquent.appointment.SqlType;
import gaarason.database.provider.ModelShadowProvider;
import gaarason.database.support.Column;
import gaarason.database.util.ObjectUtil;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import java.lang.reflect.Field;
import java.util.*;

public class BelongsToManyQueryRelation extends BaseRelationSubQuery {

    private final BelongsToManyTemplate belongsToManyTemplate;

    public BelongsToManyQueryRelation(Field field) {
        belongsToManyTemplate = new BelongsToManyTemplate(field);
    }

    @Override
    public String[] dealBatchSql(List<Map<String, Column>> stringColumnMapList,
                                 GenerateSqlPartFunctionalInterface generateSqlPart) {
        return new String[]{belongsToManyTemplate.relationModel.newQuery()
            .whereIn(belongsToManyTemplate.foreignKeyForLocalModel,
                getColumnInMapList(stringColumnMapList, belongsToManyTemplate.localModelLocalKey))
            .toSql(SqlType.SELECT), generateSqlPart.execute(belongsToManyTemplate.targetModel.newQuery()).toSql(
            SqlType.SUB_QUERY)};
    }

    @Override
    public RecordList<?, ?> dealBatch(String[] sql) {
        // 中间表结果
        List<Map<String, Object>> relationMaps = belongsToManyTemplate.relationModel.newQuery().queryList(sql[0],
            new ArrayList<>()).toMapList();

        // 将中间表结果中的目标表外键 ,转化为可以使用 where in 查询的 set
        Set<Object> targetModelForeignKeySet = new HashSet<>();
        for (Map<String, Object> map : relationMaps) {
            Object result = map.get(belongsToManyTemplate.foreignKeyForTargetModel);
            if (null == result)
                continue;
            targetModelForeignKeySet.add(result);
        }

        // 目标表结果
        RecordList<?, ?> targetRecordList = belongsToManyTemplate.targetModel.newQuery().whereRaw(sql[1])
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
    public List<?> filterBatchRecord(Record<?, ?> record, RecordList<?, ?> relationshipRecordList,
                                     Map<String, RecordList<?, ?>> cacheRelationRecordList) {
        // 目标关系表的外键字段名
        String targetModelLocalKey = belongsToManyTemplate.targetModelLocalKey;
        // 本表的关系键值
        String localModelLocalKeyValue = String.valueOf(
            record.getMetadataMap().get(belongsToManyTemplate.localModelLocalKey).getValue());

        // 本表应该关联的 目标表id列表
        Set<String> targetModelLocalKayValueSet = relationshipRecordList.getCacheMap().get(localModelLocalKeyValue);

        List<Object> objectList = new ArrayList<>();
        List<?>      objects    = relationshipRecordList.toObjectList(cacheRelationRecordList);

        if (objects.size() > 0) {
            // 模型信息
            ModelShadowProvider.ModelInfo<?, ?> modelInfo = ModelShadowProvider.get(
                relationshipRecordList.get(0).getModel());
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
    public void attach(Record<?, ?> record, RecordList<?, ?> targetRecords, Map<String, String> stringStringMap) {
        // 本表的关系键值
        String localModelLocalKeyValue = String.valueOf(
            record.getMetadataMap().get(belongsToManyTemplate.localModelLocalKey).getValue());
        // 目标表的关系键值列表
        List<Object> targetModelLocalKeyValueList = targetRecords.toList(
            recordTemp -> String.valueOf(
                recordTemp.getMetadataMap().get(belongsToManyTemplate.targetModelLocalKey).getValue()));

        // 事物
        belongsToManyTemplate.relationModel.newQuery().transaction(() -> {

            // 查询是否存在已经存在对应的关系
            RecordList<?, ?> records = belongsToManyTemplate.relationModel.newQuery()
                .select(belongsToManyTemplate.foreignKeyForLocalModel,
                    belongsToManyTemplate.foreignKeyForTargetModel)
                .where(belongsToManyTemplate.localModelLocalKey, localModelLocalKeyValue)
                .whereIn(belongsToManyTemplate.targetModelLocalKey, targetModelLocalKeyValueList)
                .get();

            // 剔除已经存在的关系, 保留需要插入的ids
            List<Object> insertTargetModelLocalKeyValueList =
                records.toList(recordTemp -> {
                    String insertTargetModelLocalKeyValue = String.valueOf(
                        recordTemp.getMetadataMap().get(belongsToManyTemplate.targetModelLocalKey).getValue());
                    if (!targetModelLocalKeyValueList.contains(insertTargetModelLocalKeyValue)) {
                        return insertTargetModelLocalKeyValue;
                    } else return null;
                });

            // 预处理中间表信息
            Set<String>  middleColumnSet = stringStringMap.keySet();
            List<String> middleValueList = new ArrayList<>();
            for (String column : middleColumnSet) {
                middleValueList.add(stringStringMap.get(column));
            }

            // 格式化, 并附带需要存储到中间表的信息
            List<String> columnList = new ArrayList<>();
            columnList.add(belongsToManyTemplate.foreignKeyForLocalModel);
            columnList.add(belongsToManyTemplate.foreignKeyForTargetModel);
            columnList.addAll(middleColumnSet);

            List<List<String>> valuesList = new ArrayList<>();
            for (Object o : insertTargetModelLocalKeyValueList) {
                List<String> valueList = new ArrayList<>();
                valueList.add(localModelLocalKeyValue);
                valueList.add(o.toString());
                // 加入中间表数据
                valueList.addAll(middleValueList);
                valuesList.add(valueList);
            }

            // 插入
            belongsToManyTemplate.relationModel.newQuery().select(columnList).valueList(valuesList).insert();

        }, 3, true);

    }

    static class BelongsToManyTemplate {
        Model<?, ?> relationModel; // user_teacher

        String foreignKeyForLocalModel;// user_id

        String localModelLocalKey; // user.id

        Model<?, ?> targetModel; // teacher

        String foreignKeyForTargetModel; // teacher_id

        String targetModelLocalKey;  // teacher.id

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
