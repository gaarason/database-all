package gaarason.database.eloquent.relations;

import gaarason.database.contracts.function.GenerateSqlPart;
import gaarason.database.contracts.function.RelationshipRecordWith;
import gaarason.database.eloquent.Model;
import gaarason.database.eloquent.Record;
import gaarason.database.eloquent.RecordList;
import gaarason.database.eloquent.annotations.BelongsToMany;
import gaarason.database.support.Column;
import gaarason.database.support.RecordFactory;
import gaarason.database.utils.EntityUtil;

import java.lang.reflect.Field;
import java.util.*;

public class BelongsToManyQuery extends BaseSubQuery {

    final public static String RELATION_REMEMBER_KEY = "RELATION_REMEMBER_KEY";

    private final BelongsToManyTemplate belongsToManyTemplate;

    private final Map<String , Set<String>> ppp = new HashMap<>();

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

    /**
     * 批量关联查询
     * @param stringColumnMapList    当前recordList的元数据
     * @param generateSqlPart        Builder
     * @param relationshipRecordWith Record
     * @return 查询结果集
     */
    @Override
    public RecordList<?, ?> dealBatch(List<Map<String, Column>> stringColumnMapList, GenerateSqlPart generateSqlPart,
                                      RelationshipRecordWith relationshipRecordWith) {
        // 中间表
        List<Map<String, Object>> relationMaps = belongsToManyTemplate.relationModel.newQuery()
            .whereIn(belongsToManyTemplate.foreignKeyForLocalModel, getColumnInMapList(stringColumnMapList,
                belongsToManyTemplate.localModelLocalKey))
            .get().toMapList();

        Set<Object> targetModelForeignKeySet = new HashSet<>();
        for (Map<String, Object> map : relationMaps) {
            Object result = map.get(belongsToManyTemplate.foreignKeyForTargetModel);
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

            // 与 此record存在关系的 目标关系表的关系键值
            Set<String> relationIds = new HashSet<>();

            for (Map<String, Object> map : relationMaps) {
                Object modelForeignKeyInMap       = map.get(belongsToManyTemplate.foreignKeyForLocalModel);
                Object targetModelForeignKeyInMap = map.get(belongsToManyTemplate.foreignKeyForTargetModel);
                if (modelForeignKeyInMap != null && targetModelForeignKeyInMap.toString()
                    .equals(
                        record.getMetadataMap().get(belongsToManyTemplate.targetModelLocalKey).getValue().toString())) {
                    relationIds.add(modelForeignKeyInMap.toString());
                }
            }
            //
            ppp.put(record.getMetadataMap().get(belongsToManyTemplate.localModelLocalKey).getValue().toString(),
                relationIds);


            column.setRelationIds(relationIds);

            // 记录标记
            record.getMetadataMap().put(RELATION_REMEMBER_KEY, column);
        }
        return targetRecordList;
    }

    @Override
    public List<?> filterBatchRecord(Record<?, ?> record, List<?> relationshipObjectList) {

        // 目标关系表的外键字段名
        String       column     = belongsToManyTemplate.localModelLocalKey;
        // 本表的关系键值
        String       value      = String.valueOf(
            record.getMetadataMap().get(belongsToManyTemplate.localModelLocalKey).getValue());
        Set<String> strings = ppp.get(value);


        List<Object> objectList = new ArrayList<>();

        for (Object o : relationshipObjectList) {
            // todo 有优化空间
            Object fieldByColumn = EntityUtil.getFieldByColumn(o, column);

            // 满足则加入
            // todo
            if (value.equals(fieldByColumn.toString()) || (strings != null && !strings.isEmpty() && strings.contains(value))) {
                // 加入
                objectList.add(o);
            }
        }
        return objectList;

    }
}
