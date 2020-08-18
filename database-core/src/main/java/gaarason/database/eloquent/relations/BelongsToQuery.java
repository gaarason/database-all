package gaarason.database.eloquent.relations;

import gaarason.database.contracts.function.GenerateSqlPart;
import gaarason.database.contracts.function.RelationshipRecordWith;
import gaarason.database.eloquent.Model;
import gaarason.database.eloquent.Record;
import gaarason.database.eloquent.RecordList;
import gaarason.database.eloquent.annotations.BelongsTo;
import gaarason.database.support.Column;
import gaarason.database.utils.EntityUtil;

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

        return generateSqlPart.generate(belongsToTemplate.parentModel.newQuery())
            .whereIn(belongsToTemplate.parentModelLocalKey,
                getColumnInMapList(stringColumnMapList, belongsToTemplate.localModelForeignKey))
            .get();
    }

    @Override
    public List<?> filterBatchRecord(Record<?, ?> record, List<?> relationshipObjectList) {
        // 父表的外键字段名
        String column = belongsToTemplate.parentModelLocalKey;
        // 本表的关系键值
        String value = String.valueOf(
            record.getMetadataMap().get(belongsToTemplate.localModelForeignKey).getValue());
        List<Object> objectList = new ArrayList<>();
        for (Object o : relationshipObjectList) {
            // todo 有优化空间
            Object fieldByColumn = EntityUtil.getFieldByColumn(o, column);

            // 满足则加入
            if (value.equals(fieldByColumn.toString())) {
                // 加入
                objectList.add(o);
            }
        }
        return objectList;

    }
}
