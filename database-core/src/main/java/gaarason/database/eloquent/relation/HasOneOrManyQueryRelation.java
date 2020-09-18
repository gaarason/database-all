package gaarason.database.eloquent.relation;

import gaarason.database.contract.eloquent.Model;
import gaarason.database.contract.eloquent.Record;
import gaarason.database.contract.eloquent.RecordList;
import gaarason.database.contract.function.GenerateSqlPartFunctionalInterface;
import gaarason.database.eloquent.annotation.HasOneOrMany;
import gaarason.database.eloquent.appointment.SqlType;
import gaarason.database.support.Column;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class HasOneOrManyQueryRelation extends BaseRelationSubQuery {

    private final HasOneOrManyTemplate hasOneOrManyTemplate;

    public HasOneOrManyQueryRelation(Field field) {
        hasOneOrManyTemplate = new HasOneOrManyTemplate(field);
    }

    @Override
    public String[] prepareSqlArr(List<Map<String, Column>> stringColumnMapList,
                                  GenerateSqlPartFunctionalInterface generateSqlPart) {
        return new String[]{generateSqlPart.execute(hasOneOrManyTemplate.sonModel.newQuery())
            .whereIn(hasOneOrManyTemplate.sonModelForeignKey,
                getColumnInMapList(stringColumnMapList, hasOneOrManyTemplate.localModelLocalKey))
            .toSql(SqlType.SELECT), ""};
    }

    @Override
    public RecordList<?, ?> dealBatch(String sql0, RecordList<?, ?> relationRecordList) {
        return hasOneOrManyTemplate.sonModel.newQuery().queryList(sql0, new ArrayList<>());
    }

    @Override
    public List<?> filterBatchRecord(Record<?, ?> record, RecordList<?, ?> TargetRecordList,
                                     Map<String, RecordList<?, ?>> cacheRelationRecordList) {
        // 子表的外键字段名
        String column = hasOneOrManyTemplate.sonModelForeignKey;
        // 本表的关系键值
        String value = String.valueOf(record.getMetadataMap().get(hasOneOrManyTemplate.localModelLocalKey).getValue());

        return findObjList(TargetRecordList.toObjectList(cacheRelationRecordList), column, value);
    }

    @Override
    public void attach(Record<?, ?> record, RecordList<?, ?> targetRecords, Map<String, String> stringStringMap) {
        // 应该更新的子表的主键列表
        List<String> targetRecordPrimaryKeyIds = targetRecords.toList(
            recordTemp -> String.valueOf(
                recordTemp.getMetadataMap().get(recordTemp.getModel().getPrimaryKeyColumnName()).getValue()));

        attach(record, targetRecordPrimaryKeyIds, stringStringMap);

    }

    @Override
    public void attach(Record<?, ?> record, Collection<String> targetPrimaryKeyValues,
                       Map<String, String> stringStringMap) {
        if (targetPrimaryKeyValues.size() == 0)
            return;

        // 当前表(子表)的关联键值
        String relationKeyValue = String.valueOf(
            record.getMetadataMap().get(record.getModel().getPrimaryKeyColumnName()).getValue());

        // 执行插入
        hasOneOrManyTemplate.sonModel.newQuery()
            .whereIn(hasOneOrManyTemplate.sonModel.getPrimaryKeyColumnName(), targetPrimaryKeyValues)
            .data(hasOneOrManyTemplate.sonModelForeignKey, relationKeyValue).update();

    }


    static class HasOneOrManyTemplate {
        final Model<?, ?> sonModel;

        final String sonModelForeignKey;

        final String localModelLocalKey;

        HasOneOrManyTemplate(Field field) {
            HasOneOrMany hasOneOrMany = field.getAnnotation(HasOneOrMany.class);
            sonModel = getModelInstance(field);
            sonModelForeignKey = hasOneOrMany.sonModelForeignKey();
            localModelLocalKey = "".equals(hasOneOrMany.localModelLocalKey())
                ? sonModel.getPrimaryKeyColumnName()
                : hasOneOrMany.localModelLocalKey();

        }
    }

}
