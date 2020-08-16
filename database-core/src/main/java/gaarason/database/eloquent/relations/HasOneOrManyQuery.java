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
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class HasOneOrManyQuery extends BaseSubQuery {

    private final HasOneOrManyTemplate hasOneOrManyTemplate;

    private final boolean isCollection;

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

    public HasOneOrManyQuery(Field field){
        hasOneOrManyTemplate = new HasOneOrManyTemplate(field);
        isCollection = Arrays.asList(field.getType().getInterfaces()).contains(Collection.class);

    }

    /**
     * 批量关联查询
     * @param stringColumnMapList    当前recordList的元数据
     * @param generateSqlPart        Builder
     * @param relationshipRecordWith Record
     * @return 查询结果集
     */
    @Override
    public RecordList<?, ?> dealBatch(List<Map<String, Column>> stringColumnMapList,
                                      GenerateSqlPart generateSqlPart,
                                      RelationshipRecordWith relationshipRecordWith) {
        RecordList<?, ?> records = generateSqlPart.generate(hasOneOrManyTemplate.sonModel.newQuery())
//            .whereIn(hasOneOrManyTemplate.localModelLocalKey, getColumnInMapList(stringColumnMapList, hasOneOrManyTemplate.sonModelForeignKey))
// todo check
            .whereIn(hasOneOrManyTemplate.sonModelForeignKey, getColumnInMapList(stringColumnMapList, hasOneOrManyTemplate.localModelLocalKey))


            .get();
//        for (Record<?, ?> record : records) {
//            relationshipRecordWith.generate(record);
//        }
        return records;
    }

    /**
     * 筛选批量关联查询结果
     * @param record                 当前record
     * @param relationshipRecordList 关联的recordList
     * @return 筛选后的查询结果集
     */
    @Nullable
    @Override
    public Object filterBatch(Record<?, ?> record,
                              RecordList<?, ?> relationshipRecordList,
                              Map<String, RecordList<?, ?>> cacheRelationRecordList) {
        if(isCollection){

//        return RecordFactory.filterRecordList(relationshipRecordList, hasMany.localKey,
//            String.valueOf(record.getMetadataMap().get(hasMany.foreignKey).getValue())).toObjectList();

        // todo  check
        return RecordFactory.filterRecordList(relationshipRecordList, hasOneOrManyTemplate.sonModelForeignKey,
            String.valueOf(record.getMetadataMap().get(hasOneOrManyTemplate.localModelLocalKey).getValue())).toObjectList(cacheRelationRecordList);

        }else{
//            Record<?, ?> newRecord = RecordFactory.filterRecord(relationshipRecordList, hasOneOrManyTemplate.localModelLocalKey,
//                String.valueOf(record.getMetadataMap().get(hasOneOrManyTemplate.sonModelForeignKey).getValue()));
//            return newRecord == null ? null : newRecord.toObject(cacheRelationRecordList);
            Record<?, ?> newRecord = RecordFactory.filterRecord(relationshipRecordList, hasOneOrManyTemplate.sonModelForeignKey,
                String.valueOf(record.getMetadataMap().get(hasOneOrManyTemplate.localModelLocalKey).getValue()));
            return newRecord == null ? null : newRecord.toObject(cacheRelationRecordList);
        }

    }

}
