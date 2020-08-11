package gaarason.database.eloquent.relations;

import gaarason.database.contracts.function.GenerateSqlPart;
import gaarason.database.contracts.function.RelationshipRecordWith;
import gaarason.database.core.lang.Nullable;
import gaarason.database.eloquent.Model;
import gaarason.database.eloquent.Record;
import gaarason.database.eloquent.RecordList;
import gaarason.database.eloquent.annotations.HasOneOrMany;
import gaarason.database.support.Column;
import gaarason.database.support.RecordFactory;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

public class HasOneOrManyQuery extends BaseSubQuery {

    static class HasOneOrManyTemplate {
        Model<?, ?> sonModel;

        String sonModelForeignKey;

        String localModelLocalKey;

        HasOneOrManyTemplate(Field field) {
            HasOneOrMany hasOneOrMany = field.getAnnotation(HasOneOrMany.class);
            sonModel = getModelInstance(hasOneOrMany.sonModel());
            sonModelForeignKey = hasOneOrMany.sonModelForeignKey();
            localModelLocalKey = hasOneOrMany.localModelLocalKey();
            localModelLocalKey = "".equals(
                localModelLocalKey) ? sonModel.getPrimaryKeyColumnName() : localModelLocalKey;

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
//    @Nullable
//    public static Object dealSingle(Field field, Map<String, Column> stringColumnMap,
//                                    GenerateSqlPart generateSqlPart,
//                                    RelationshipRecordWith relationshipRecordWith) {
//        HasOneTemplate hasOne = new HasOneTemplate(field);
//        Record<?, ?> record = generateSqlPart.generate(hasOne.targetModel.newQuery())
//            .where(hasOne.localKey,
//                String.valueOf(stringColumnMap.get(hasOne.foreignKey).getValue()))
//            .first();
//
//        //  todo  这里约等于递归
//        return record == null ? null : relationshipRecordWith.generate(record).toObject();
//    }
//
//
//    /**
//     * 单个关联查询, 以及对象转化
//     * @param field                  字段
//     * @param stringColumnMap        当前record的元数据
//     * @param generateSqlPart        Builder
//     * @param relationshipRecordWith Record
//     * @param tableCache             缓存
//     * @return 目标实体对象
//     */
//    @Nullable
//    public static Object dealSingle(Field field, Map<String, Column> stringColumnMap,
//                                      GenerateSqlPart generateSqlPart,
//                                      RelationshipRecordWith relationshipRecordWith,
//                                      TableCache<Record<?, ?>> tableCache) {
//        HasOneTemplate hasOne = new HasOneTemplate(field);
//        Record<?, ?> record = generateSqlPart.generate(hasOne.targetModel.newQuery())
//            .where(hasOne.localKey,
//                String.valueOf(stringColumnMap.get(hasOne.foreignKey).getValue()))
//            .first();
//        return record == null ? null : relationshipRecordWith.generate(record).toObject();
//    }

    /**
     * 批量关联查询
     * @param field                  字段
     * @param stringColumnMapList    当前recordList的元数据
     * @param generateSqlPart        Builder
     * @param relationshipRecordWith Record
     * @return 查询结果集
     */
    public RecordList<?, ?> dealBatch(Field field, List<Map<String, Column>> stringColumnMapList,
                                      GenerateSqlPart generateSqlPart,
                                      RelationshipRecordWith relationshipRecordWith) {
        HasOneOrManyTemplate hasOne = new HasOneOrManyTemplate(field);
        RecordList<?, ?> records = generateSqlPart.generate(hasOne.sonModel.newQuery())
            .whereIn(hasOne.localModelLocalKey, getColumnInMapList(stringColumnMapList, hasOne.sonModelForeignKey))
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
    @Nullable
    public Object filterBatch(Field field, Record<?, ?> record,
                              RecordList<?, ?> relationshipRecordList,
                              Map<String, RecordList<?, ?>> cacheRelationRecordList) {

        Class<?> fieldType = field.getType();

        HasOneOrManyTemplate hasOne = new HasOneOrManyTemplate(field);
        Record<?, ?> newRecord = RecordFactory.filterRecord(relationshipRecordList, hasOne.localModelLocalKey,
            String.valueOf(record.getMetadataMap().get(hasOne.sonModelForeignKey).getValue()));

        return newRecord == null ? null : newRecord.toObject(cacheRelationRecordList);

        // todo  return list

    }

//    /**
//     * 筛选批量关联查询结果
//     * @param field                  字段
//     * @param record                 当前record
//     * @param relationshipRecordList 关联的recordList
//     * @return 筛选后的查询结果集
//     */
//    @Nullable
//    public static Object filterBatch(Field field, Record<?, ?> record,
//                                     RecordList<?, ?> relationshipRecordList, Map<String, RecordList<?, ?>> cacheRelationRecordList) {
//        HasOneTemplate hasOne = new HasOneTemplate(field);
//        Record<?, ?> newRecord = RecordFactory.filterRecord(relationshipRecordList, hasOne.localKey,
//            String.valueOf(record.getMetadataMap().get(hasOne.foreignKey).getValue()));
//
//        return newRecord == null ? null : newRecord.toObject(cacheRelationRecordList);
//
//    }
//
//    /**
//     * 筛选批量关联查询结果
//     * @param field                  字段
//     * @param record                 当前record
//     * @param relationshipRecordList 关联的recordList
//     * @return 筛选后的查询结果集
//     */
//    public static List<?> filterBatch(Field field, Record<?, ?> record,
//                                      RecordList<?, ?> relationshipRecordList, Map<String, RecordList<?, ?>> cacheRelationRecordList) {
////        // 筛选当前 record 所需要的属性
////        HasMany     hasMany     = field.getAnnotation(HasMany.class);
////        Model<?, ?> targetModel = getModelInstance(hasMany.targetModel());
////        String      foreignKey  = hasMany.foreignKey();
////        String      localKey    = hasMany.localKey();
////        localKey = "".equals(localKey) ? targetModel.getPrimaryKeyColumnName() : localKey;
//        HasManyQuery.HasManyTemplate hasMany = new HasManyQuery.HasManyTemplate(field);
//
////        return RecordFactory.filterRecordList(relationshipRecordList, hasMany.localKey,
////            String.valueOf(record.getMetadataMap().get(hasMany.foreignKey).getValue())).toObjectList();
//
//
//        // todo  check
//        return RecordFactory.filterRecordList(relationshipRecordList, hasMany.foreignKey,
//            String.valueOf(record.getMetadataMap().get(hasMany.localKey).getValue())).toObjectList(cacheRelationRecordList);
//    }

}
