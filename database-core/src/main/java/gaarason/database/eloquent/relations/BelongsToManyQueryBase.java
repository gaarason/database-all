package gaarason.database.eloquent.relations;

import gaarason.database.contracts.function.GenerateSqlPart;
import gaarason.database.contracts.function.RelationshipRecordWith;
import gaarason.database.eloquent.Model;
import gaarason.database.eloquent.Record;
import gaarason.database.eloquent.RecordList;
import gaarason.database.eloquent.annotations.BelongsToMany;
import gaarason.database.support.Column;
import gaarason.database.support.RecordFactory;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BelongsToManyQueryBase extends BaseSubQuery {

    final public static String RELATION_REMEMBER_KEY = "RELATION_REMEMBER_KEY";

    static class BelongsToManyTemplate {
        Model<?, ?> relationModel; // user_teacher

        String modelForeignKey;// user_id

        String modelLocalKey; // user.id

        Model<?, ?> targetModel; // teacher

        String targetModelForeignKey; // teacher_id

        String targetModelLocalKey;  // teacher.id

        BelongsToManyTemplate(Field field) {
            BelongsToMany belongsToMany = field.getAnnotation(BelongsToMany.class);
            relationModel = getModelInstance(belongsToMany.relationModel()); // user_teacher
            modelForeignKey = belongsToMany.modelForeignKey(); // user_id
            modelLocalKey = belongsToMany.modelLocalKey(); // user.id
            targetModel = getModelInstance(belongsToMany.targetModel()); // teacher
            targetModelForeignKey = belongsToMany.targetModelForeignKey(); // teacher_id
            targetModelLocalKey = belongsToMany.targetModelLocalKey();  // teacher.id
        }
    }

//    /**
//     * 单个关联查询, 以及对象转化
//     * @param field                  字段
//     * @param stringColumnMap        当前record的元数据
//     * @param generateSqlPart        Builder
//     * @param relationshipRecordWith Record
//     * @return 目标实体对象
//     */
//    public static List<?> dealSingle(Field field, Map<String, Column> stringColumnMap,
//                                     GenerateSqlPart generateSqlPart,
//                                     RelationshipRecordWith relationshipRecordWith) {
//
//        BelongsToManyTemplate belongsToMany = new BelongsToManyTemplate(field);
//
//        // 中间表
//        List<Object> targetModelForeignKeyList = belongsToMany.relationModel.newQuery()
//            .where(belongsToMany.modelForeignKey,
//                String.valueOf(stringColumnMap.get(belongsToMany.modelLocalKey).getValue()))
//            .get()
//            .toList(record -> record.toMap().get(belongsToMany.targetModelForeignKey));
//        // 目标表
//        RecordList<?, ?> relationRecordList = generateSqlPart.generate(belongsToMany.targetModel.newQuery())
//            .whereIn(belongsToMany.targetModelLocalKey, targetModelForeignKeyList)
//            .get();
//        for (Record<?, ?> record : relationRecordList) {
//            relationshipRecordWith.generate(record);
//        }
//        return relationRecordList.toObjectList();
//    }

    /**
     * 批量关联查询
     * @param field                  字段
     * @param metadataMap            当前record的元数据
     * @param stringColumnMapList    当前recordList的元数据
     * @param generateSqlPart        Builder
     * @param relationshipRecordWith Record
     * @return 查询结果集
     */
    public static RecordList<?, ?> dealBatch(Field field, Map<String, Column> metadataMap,
                                             List<Map<String, Column>> stringColumnMapList,
                                             GenerateSqlPart generateSqlPart,
                                             RelationshipRecordWith relationshipRecordWith) {
        BelongsToManyTemplate belongsToMany = new BelongsToManyTemplate(field);
        // 中间表
        List<Map<String, Object>> maps = belongsToMany.relationModel.newQuery()
            .whereIn(belongsToMany.modelForeignKey, getColumnInMapList(stringColumnMapList,
                belongsToMany.modelLocalKey))
            .get().toMapList();

        Set<Object> targetModelForeignKeySet = new HashSet<>();
        for (Map<String, Object> map : maps) {
            Object result = map.get(belongsToMany.targetModelForeignKey);
            if (null == result)
                continue;
            targetModelForeignKeySet.add(result);
        }

        // 目标表
        RecordList<?, ?> targetRecordList = generateSqlPart.generate(belongsToMany.targetModel.newQuery())
            .whereIn(belongsToMany.targetModelLocalKey, targetModelForeignKeySet)
            .get();
        for (Record<?, ?> record : targetRecordList) {
            // 产生标记
            Column column = new Column();
            column.setColumnName(RELATION_REMEMBER_KEY);
            column.setValue("");
            Set<String> relationIds = new HashSet<>();

            for (Map<String, Object> map : maps) {
                Object modelForeignKeyInMap       = map.get(belongsToMany.modelForeignKey);
                Object targetModelForeignKeyInMap = map.get(belongsToMany.targetModelForeignKey);
                if (modelForeignKeyInMap != null && targetModelForeignKeyInMap.toString()
                    .equals(record.getMetadataMap().get(belongsToMany.targetModelLocalKey).getValue().toString())) {
                    relationIds.add(modelForeignKeyInMap.toString());
                }
            }
            column.setRelationIds(relationIds);

            // 记录标记
            record.getMetadataMap().put(RELATION_REMEMBER_KEY, column);
            relationshipRecordWith.generate(record);
        }
        return targetRecordList;
    }

    /**
     * 筛选批量关联查询结果
     * @param field                  字段
     * @param record                 当前record
     * @param relationshipRecordList 关联的recordList
     * @return 筛选后的查询结果集
     */
    public static List<?> filterBatch(Field field, Record<?, ?> record,
                                      RecordList<?, ?> relationshipRecordList, Map<String, RecordList<?, ?>> cacheRelationRecordList) {
        // 筛选当前 record 所需要的属性
        BelongsToManyTemplate belongsToMany = new BelongsToManyTemplate(field);

        return RecordFactory.filterRecordList(relationshipRecordList, RELATION_REMEMBER_KEY,
            String.valueOf(record.getMetadataMap().get(belongsToMany.modelLocalKey).getValue())).toObjectList(cacheRelationRecordList);
    }
}
