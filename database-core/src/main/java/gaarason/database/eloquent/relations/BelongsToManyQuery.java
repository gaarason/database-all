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

public class BelongsToManyQuery extends BaseSubQuery {

    final public static String RELATION_REMEMBER_KEY = "RELATION_REMEMBER_KEY";

    private final BelongsToManyTemplate belongsToManyTemplate;

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

    public BelongsToManyQuery(Field field){
        belongsToManyTemplate = new BelongsToManyTemplate(field);
    }

    /**
     * 批量关联查询
     * @param stringColumnMapList    当前recordList的元数据
     * @param generateSqlPart        Builder
     * @param relationshipRecordWith Record
     * @return 查询结果集
     */
    @Override
    public RecordList<?, ?> dealBatch(List<Map<String, Column>> stringColumnMapList, GenerateSqlPart generateSqlPart, RelationshipRecordWith relationshipRecordWith) {
        // 中间表
        List<Map<String, Object>> maps = belongsToManyTemplate.relationModel.newQuery()
                .whereIn(belongsToManyTemplate.modelForeignKey, getColumnInMapList(stringColumnMapList,
                        belongsToManyTemplate.modelLocalKey))
                .get().toMapList();

        Set<Object> targetModelForeignKeySet = new HashSet<>();
        for (Map<String, Object> map : maps) {
            Object result = map.get(belongsToManyTemplate.targetModelForeignKey);
            if (null == result)
                continue;
            targetModelForeignKeySet.add(result);
        }

        // 目标表
        RecordList<?, ?> targetRecordList = generateSqlPart.generate(belongsToManyTemplate.targetModel.newQuery())
                .whereIn(belongsToManyTemplate.targetModelLocalKey, targetModelForeignKeySet)
                .get();
        for (Record<?, ?> record : targetRecordList) {
            // 产生标记
            Column column = new Column();
            column.setColumnName(RELATION_REMEMBER_KEY);
            column.setValue("");
            Set<String> relationIds = new HashSet<>();

            for (Map<String, Object> map : maps) {
                Object modelForeignKeyInMap       = map.get(belongsToManyTemplate.modelForeignKey);
                Object targetModelForeignKeyInMap = map.get(belongsToManyTemplate.targetModelForeignKey);
                if (modelForeignKeyInMap != null && targetModelForeignKeyInMap.toString()
                        .equals(record.getMetadataMap().get(belongsToManyTemplate.targetModelLocalKey).getValue().toString())) {
                    relationIds.add(modelForeignKeyInMap.toString());
                }
            }
            column.setRelationIds(relationIds);

            // 记录标记
            record.getMetadataMap().put(RELATION_REMEMBER_KEY, column);
//            relationshipRecordWith.generate(record);
        }
        return targetRecordList;
    }

    /**
     * 筛选批量关联查询结果
     * @param record                 当前record
     * @param relationshipRecordList 关联的recordList
     * @return 筛选后的查询结果集
     */
    @Override
    public List<?> filterBatch(Record<?, ?> record,
                                      RecordList<?, ?> relationshipRecordList, Map<String, RecordList<?, ?>> cacheRelationRecordList) {

        return RecordFactory.filterRecordList(relationshipRecordList, RELATION_REMEMBER_KEY,
            String.valueOf(record.getMetadataMap().get(belongsToManyTemplate.modelLocalKey).getValue())).toObjectList(cacheRelationRecordList);
    }
}
