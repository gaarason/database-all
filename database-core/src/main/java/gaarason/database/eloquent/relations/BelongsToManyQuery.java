package gaarason.database.eloquent.relations;

import gaarason.database.contracts.function.GenerateSqlPart;
import gaarason.database.eloquent.Model;
import gaarason.database.eloquent.Record;
import gaarason.database.eloquent.RecordList;
import gaarason.database.eloquent.annotations.BelongsToMany;
import gaarason.database.eloquent.enums.SqlType;
import gaarason.database.support.Column;
import gaarason.database.utils.EntityUtil;

import java.lang.reflect.Field;
import java.util.*;

public class BelongsToManyQuery extends BaseSubQuery {

    private final BelongsToManyTemplate belongsToManyTemplate;

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
            targetModel = getModelInstance(belongsToMany.targetModel()); // teacher
            foreignKeyForTargetModel = belongsToMany.foreignKeyForTargetModel(); // teacher_id
            targetModelLocalKey = belongsToMany.targetModelLocalKey();  // teacher.id
        }
    }

    public BelongsToManyQuery(Field field) {
        belongsToManyTemplate = new BelongsToManyTemplate(field);
    }

    @Override
    public String[] dealBatchSql(List<Map<String, Column>> stringColumnMapList, GenerateSqlPart generateSqlPart) {
        return new String[]{belongsToManyTemplate.relationModel.newQuery()
            .whereIn(belongsToManyTemplate.foreignKeyForLocalModel,
                getColumnInMapList(stringColumnMapList, belongsToManyTemplate.localModelLocalKey))
            .toSql(SqlType.SELECT), generateSqlPart.generate(belongsToManyTemplate.targetModel.newQuery()).toSql(
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

        for (Object obj : objects) {
            String targetModelLocalKeyValue = EntityUtil.getFieldValueByColumn(obj, targetModelLocalKey).toString();

            // 满足则加入
            if (targetModelLocalKayValueSet != null && targetModelLocalKayValueSet.contains(targetModelLocalKeyValue)) {
                objectList.add(obj);
            }
        }
        return objectList;
    }
}
