package gaarason.database.eloquent.relations;

import gaarason.database.contracts.function.GenerateSqlPart;
import gaarason.database.eloquent.Model;
import gaarason.database.eloquent.Record;
import gaarason.database.eloquent.RecordList;
import gaarason.database.eloquent.annotations.HasOneOrMany;
import gaarason.database.support.Column;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    public Set<Object> getSetInMapList(List<Map<String, Column>> stringColumnMapList) {
        return getColumnInMapList(stringColumnMapList, hasOneOrManyTemplate.localModelLocalKey);
    }

    @Override
    public RecordList<?, ?> dealBatch(Set<Object> setInMapList, GenerateSqlPart generateSqlPart) {
        return generateSqlPart.generate(hasOneOrManyTemplate.sonModel.newQuery())
            .whereIn(hasOneOrManyTemplate.sonModelForeignKey, setInMapList)
            .get();
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
