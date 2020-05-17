package gaarason.database.eloquent.relations;

import gaarason.database.contracts.function.GenerateSqlPart;
import gaarason.database.contracts.function.RelationshipRecordWith;
import gaarason.database.core.lang.Nullable;
import gaarason.database.eloquent.Model;
import gaarason.database.eloquent.Record;
import gaarason.database.eloquent.RecordList;
import gaarason.database.eloquent.annotations.HasOne;
import gaarason.database.support.Column;
import gaarason.database.support.RecordFactory;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

public class HasOneQuery extends SubQuery {

    static class HasOneTemplate<T, K> {
        Model<T, K> targetModel;

        String      foreignKey;

        String      localKey;

        HasOneTemplate(Field field) {
            HasOne hasOne = field.getAnnotation(HasOne.class);
            targetModel = getModelInstance(hasOne.targetModel());
            foreignKey = hasOne.foreignKey();
            localKey = hasOne.localKey();
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
    @Nullable
    public static <T, K> T dealSingle(Field field, Map<String, Column> stringColumnMap,
                                      GenerateSqlPart<T, K> generateSqlPart,
                                      RelationshipRecordWith<T, K> relationshipRecordWith) {
        HasOneTemplate<T, K> hasOne = new HasOneTemplate<>(field);
        Record<T, K> record = generateSqlPart.generate(hasOne.targetModel.newQuery())
            .where(hasOne.localKey,
                String.valueOf(stringColumnMap.get(hasOne.foreignKey).getValue()))
            .first();
        return record == null ? null : relationshipRecordWith.generate(record).toObject();
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
        HasOneTemplate<T, K> hasOne = new HasOneTemplate<>(field);
        RecordList<T, K> records = generateSqlPart.generate(hasOne.targetModel.newQuery())
            .whereIn(hasOne.localKey, getColumnInMapList(stringColumnMapList, hasOne.foreignKey))
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
    @Nullable
    public static <T, K> Object filterBatch(Field field, Record<?, ?> record,
                                            RecordList<?, ?> relationshipRecordList) {
        HasOneTemplate<T, K> hasOne = new HasOneTemplate<>(field);
        Record<?, ?> newRecord = RecordFactory.filterRecord(relationshipRecordList, hasOne.localKey,
            String.valueOf(record.getMetadataMap().get(hasOne.foreignKey).getValue()));

        return newRecord == null ? null : newRecord.toObject();

    }
}
