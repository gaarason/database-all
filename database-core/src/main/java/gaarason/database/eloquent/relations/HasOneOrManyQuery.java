package gaarason.database.eloquent.relations;

import gaarason.database.contracts.function.GenerateSqlPart;
import gaarason.database.eloquent.Model;
import gaarason.database.eloquent.Record;
import gaarason.database.eloquent.RecordList;
import gaarason.database.eloquent.annotations.HasOneOrMany;
import gaarason.database.eloquent.enums.SqlType;
import gaarason.database.support.Column;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HasOneOrManyQuery extends BaseSubQuery {

    private final HasOneOrManyTemplate hasOneOrManyTemplate;

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

    public HasOneOrManyQuery(Field field) {
        hasOneOrManyTemplate = new HasOneOrManyTemplate(field);
    }

    @Override
    public String[] dealBatchSql(List<Map<String, Column>> stringColumnMapList, GenerateSqlPart generateSqlPart) {
        return new String[]{generateSqlPart.generate(hasOneOrManyTemplate.sonModel.newQuery())
            .whereIn(hasOneOrManyTemplate.sonModelForeignKey,
                getColumnInMapList(stringColumnMapList, hasOneOrManyTemplate.localModelLocalKey))
            .toSql(SqlType.SELECT)};
    }

    @Override
    public RecordList<?, ?> dealBatch(String[] sql) {
        return hasOneOrManyTemplate.sonModel.newQuery().queryList(sql[0], new ArrayList<>());
    }

    @Override
    public List<?> filterBatchRecord(Record<?, ?> record, RecordList<?, ?> relationshipRecordList,
                                     Map<String, RecordList<?, ?>> cacheRelationRecordList) {
        // 子表的外键字段名
        String column = hasOneOrManyTemplate.sonModelForeignKey;
        // 本表的关系键值
        String value = String.valueOf(record.getMetadataMap().get(hasOneOrManyTemplate.localModelLocalKey).getValue());

        return findObjList(relationshipRecordList.toObjectList(cacheRelationRecordList), column, value);
    }

}
