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

public class HasManyQuery extends SubQuery {

    static class HasManyTemplate<T, K> {
        Model<T, K> targetModel;

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

    /**
     * 单个关联查询, 以及对象转化
     * @param field                  字段
     * @param stringColumnMap        当前record的元数据
     * @param generateSqlPart        Builder
     * @param relationshipRecordWith Record
     * @param <T>                    目标实体类
     * @param <K>                    目标实体主键
     * @return 目标实体对象
     */
    public static <T, K> List<T> dealSingle(Field field, Map<String, Column> stringColumnMap,
                                            GenerateSqlPart<T, K> generateSqlPart,
                                            RelationshipRecordWith<T, K> relationshipRecordWith) {
        HasManyQuery.HasManyTemplate<T, K> hasMany = new HasManyQuery.HasManyTemplate<>(field);


//        HasMany     hasMany     = field.getAnnotation(HasMany.class);
//        Model<T, K> targetModel = getModelInstance(hasMany.targetModel());
//        String      foreignKey  = hasMany.foreignKey();
//        String      localKey    = hasMany.localKey();
//        localKey = "".equals(localKey) ? targetModel.getPrimaryKeyColumnName() : localKey;
        RecordList<T, K> records = generateSqlPart.generate(hasMany.targetModel.newQuery())
            .where(hasMany.foreignKey, String.valueOf(stringColumnMap.get(hasMany.localKey).getValue()))
            .get();
        for (Record<T, K> record : records) {
            relationshipRecordWith.generate(record);
        }
        return records.toObjectList();
    }

    /**
     * 批量关联查询
     * @param field                  字段
     * @param stringColumnMapList    当前recordList的元数据
     * @param generateSqlPart        Builder
     * @param relationshipRecordWith Record
     * @param <T>                    目标实体类
     * @param <K>                    目标实体主键
     * @return 查询结果集
     */
    public static <T, K> RecordList<T, K> dealBatch(Field field, List<Map<String, Column>> stringColumnMapList,
                                                    GenerateSqlPart<T, K> generateSqlPart,
                                                    RelationshipRecordWith<T, K> relationshipRecordWith) {

        HasManyQuery.HasManyTemplate<T, K> hasMany = new HasManyQuery.HasManyTemplate<>(field);
//        HasMany hasMany = field.getAnnotation(
//            HasMany.class);
//        Model<T, K> targetModel = getModelInstance(hasMany.targetModel());
//        String      foreignKey  = hasMany.foreignKey();
//        String      localKey    = hasMany.localKey();
//        localKey = "".equals(localKey) ? targetModel.getPrimaryKeyColumnName() : localKey;
        RecordList<T, K> records = generateSqlPart.generate(hasMany.targetModel.newQuery())
            .whereIn(hasMany.localKey, getColumnInMapList(stringColumnMapList, hasMany.foreignKey))
            .get();

        for (Record<T, K> record : records) {
            relationshipRecordWith.generate(record);
        }
        return records;
    }

    /**
     * 筛选批量关联查询结果
     * @param field                  字段
     * @param record                 当前record
     * @param relationshipRecordList 关联的recordList
     * @param <T>                    目标实体类
     * @param <K>                    目标实体主键
     * @return 筛选后的查询结果集
     */
    public static <T, K> List<?> filterBatch(Field field, Record<?, ?> record,
                                             RecordList<?, ?> relationshipRecordList) {
//        // 筛选当前 record 所需要的属性
//        HasMany     hasMany     = field.getAnnotation(HasMany.class);
//        Model<T, K> targetModel = getModelInstance(hasMany.targetModel());
//        String      foreignKey  = hasMany.foreignKey();
//        String      localKey    = hasMany.localKey();
//        localKey = "".equals(localKey) ? targetModel.getPrimaryKeyColumnName() : localKey;
        HasManyQuery.HasManyTemplate<T, K> hasMany = new HasManyQuery.HasManyTemplate<>(field);

        return RecordFactory.filterRecordList(relationshipRecordList, hasMany.localKey,
            String.valueOf(record.getMetadataMap().get(hasMany.foreignKey).getValue())).toObjectList();
    }
}
