package gaarason.database.eloquent.relations;

import gaarason.database.contracts.function.GenerateSqlPart;
import gaarason.database.eloquent.Model;
import gaarason.database.eloquent.Record;
import gaarason.database.eloquent.RecordList;
import gaarason.database.eloquent.annotations.BelongsTo;
import gaarason.database.eloquent.enums.SqlType;
import gaarason.database.support.Column;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BelongsToQuery extends BaseSubQuery {

    private final BelongsToTemplate belongsToTemplate;

    static class BelongsToTemplate {
        Model<?, ?> parentModel;

        String localModelForeignKey;

        String parentModelLocalKey;

        BelongsToTemplate(Field field) {
            BelongsTo belongsTo = field.getAnnotation(BelongsTo.class);
            parentModel = getModelInstance(belongsTo.parentModel());
            localModelForeignKey = belongsTo.localModelForeignKey();
            parentModelLocalKey = belongsTo.parentModelLocalKey();
            parentModelLocalKey = "".equals(
                parentModelLocalKey) ? parentModel.getPrimaryKeyColumnName() : parentModelLocalKey;
        }
    }

    public BelongsToQuery(Field field) {
        belongsToTemplate = new BelongsToTemplate(field);
    }

    @Override
    public String[] dealBatchSql(List<Map<String, Column>> stringColumnMapList, GenerateSqlPart generateSqlPart) {
        return new String[]{generateSqlPart.generate(belongsToTemplate.parentModel.newQuery())
            .whereIn(belongsToTemplate.parentModelLocalKey,
                getColumnInMapList(stringColumnMapList, belongsToTemplate.localModelForeignKey))
            .toSql(SqlType.SELECT)};
    }

    @Override
    public RecordList<?, ?> dealBatch(String[] sql) {
        return belongsToTemplate.parentModel.newQuery().queryList(sql[0], new ArrayList<>());
    }

    @Override
    public List<?> filterBatchRecord(Record<?, ?> record, RecordList<?, ?> relationshipRecordList,
                                     Map<String, RecordList<?, ?>> cacheRelationRecordList) {
        // 父表的外键字段名
        String column = belongsToTemplate.parentModelLocalKey;
        // 本表的关系键值
        String value = String.valueOf(
            record.getMetadataMap().get(belongsToTemplate.localModelForeignKey).getValue());

        return findObjList(relationshipRecordList.toObjectList(cacheRelationRecordList), column, value);
    }
}