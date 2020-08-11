package gaarason.database.eloquent.relations;

import gaarason.database.contracts.function.GenerateSqlPart;
import gaarason.database.contracts.function.RelationshipRecordWith;
import gaarason.database.eloquent.Model;
import gaarason.database.eloquent.Record;
import gaarason.database.eloquent.RecordList;
import gaarason.database.eloquent.annotations.HasMany;
import gaarason.database.support.Column;
import gaarason.database.support.RecordFactory;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

public class HasManyQueryBase extends BaseSubQuery {

    static class HasManyTemplate {
        Model<?, ?> targetModel;

        String foreignKey;

        String localKey;

        HasManyTemplate(Field field) {
            HasMany hasMany = field.getAnnotation(HasMany.class);
            targetModel = getModelInstance(hasMany.targetModel());
            foreignKey = hasMany.foreignKey();
            localKey = hasMany.localKey();
            localKey = "".equals(localKey) ? targetModel.getPrimaryKeyColumnName() : localKey;

        }
    }
//
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
//        HasManyQuery.HasManyTemplate hasMany = new HasManyQuery.HasManyTemplate(field);
//
//
////        HasMany     hasMany     = field.getAnnotation(HasMany.class);
////        Model<?, ?> targetModel = getModelInstance(hasMany.targetModel());
////        String      foreignKey  = hasMany.foreignKey();
////        String      localKey    = hasMany.localKey();
////        localKey = "".equals(localKey) ? targetModel.getPrimaryKeyColumnName() : localKey;
//        RecordList<?, ?> records = generateSqlPart.generate(hasMany.targetModel.newQuery())
//            .where(hasMany.foreignKey, String.valueOf(stringColumnMap.get(hasMany.localKey).getValue()))
//            .get();
//        for (Record<?, ?> record : records) {
//            relationshipRecordWith.generate(record);
//        }
//        return records.toObjectList();
//    }

    /**
     * 批量关联查询
     * @param field                  字段
     * @param stringColumnMapList    当前recordList的元数据
     * @param generateSqlPart        Builder
     * @param relationshipRecordWith Record
     * @return 查询结果集
     */
    public static RecordList<?, ?> dealBatch(Field field, List<Map<String, Column>> stringColumnMapList,
                                             GenerateSqlPart generateSqlPart,
                                             RelationshipRecordWith relationshipRecordWith) {

        HasManyQueryBase.HasManyTemplate hasMany = new HasManyQueryBase.HasManyTemplate(field);
//        HasMany hasMany = field.getAnnotation(
//            HasMany.class);
//        Model<?, ?> targetModel = getModelInstance(hasMany.targetModel());
//        String      foreignKey  = hasMany.foreignKey();
//        String      localKey    = hasMany.localKey();
//        localKey = "".equals(localKey) ? targetModel.getPrimaryKeyColumnName() : localKey;
        RecordList<?, ?> records = generateSqlPart.generate(hasMany.targetModel.newQuery())
//            .whereIn(hasMany.localKey, getColumnInMapList(stringColumnMapList, hasMany.foreignKey))
            // todo check
            .whereIn(hasMany.foreignKey, getColumnInMapList(stringColumnMapList, hasMany.localKey))
            .get();

        for (Record<?, ?> record : records) {
            relationshipRecordWith.generate(record);
        }
        return records;
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
//        // 筛选当前 record 所需要的属性
//        HasMany     hasMany     = field.getAnnotation(HasMany.class);
//        Model<?, ?> targetModel = getModelInstance(hasMany.targetModel());
//        String      foreignKey  = hasMany.foreignKey();
//        String      localKey    = hasMany.localKey();
//        localKey = "".equals(localKey) ? targetModel.getPrimaryKeyColumnName() : localKey;
        HasManyQueryBase.HasManyTemplate hasMany = new HasManyQueryBase.HasManyTemplate(field);

//        return RecordFactory.filterRecordList(relationshipRecordList, hasMany.localKey,
//            String.valueOf(record.getMetadataMap().get(hasMany.foreignKey).getValue())).toObjectList();


        // todo  check
        return RecordFactory.filterRecordList(relationshipRecordList, hasMany.foreignKey,
            String.valueOf(record.getMetadataMap().get(hasMany.localKey).getValue())).toObjectList(cacheRelationRecordList);
    }
}
