package gaarason.database.eloquent.relations;

import gaarason.database.contracts.function.GenerateSqlPart;
import gaarason.database.contracts.function.RelationshipRecordWith;
import gaarason.database.eloquent.Model;
import gaarason.database.eloquent.Record;
import gaarason.database.eloquent.RecordList;
import gaarason.database.eloquent.annotations.HasOneOrMany;
import gaarason.database.support.Column;
import gaarason.database.utils.EntityUtil;
import gaarason.database.utils.ObjectUtil;

import java.lang.reflect.Field;
import java.util.*;

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
        return generateSqlPart.generate(hasOneOrManyTemplate.sonModel.newQuery())
            .whereIn(hasOneOrManyTemplate.sonModelForeignKey,
                getColumnInMapList(stringColumnMapList, hasOneOrManyTemplate.localModelLocalKey))
            .get();
    }

    @Override
    public List<?> filterBatchRecord(Record<?, ?> record, List<?> relationshipObjectList) {
        // 子表的外键字段名
        String column = hasOneOrManyTemplate.sonModelForeignKey;
        // 本表的关系键值
        String value  = String.valueOf(record.getMetadataMap().get(hasOneOrManyTemplate.localModelLocalKey).getValue());

        List<Object> objectList = new ArrayList<>();

        for (Object o : relationshipObjectList) {
            // todo 有优化空间
            Object fieldByColumn = EntityUtil.getFieldByColumn(o, column);

            // 满足则加入
            if (value.equals(fieldByColumn.toString())) {
                // 加入
                objectList.add(ObjectUtil.deepCopy(o));
            }
        }
        return objectList;
    }

}
