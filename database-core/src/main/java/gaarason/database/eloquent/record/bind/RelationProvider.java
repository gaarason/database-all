package gaarason.database.eloquent.record.bind;

import gaarason.database.contract.eloquent.relation.RelationSubQuery;
import gaarason.database.contract.record.bind.Relation;
import gaarason.database.eloquent.Record;
import gaarason.database.eloquent.RecordList;
import gaarason.database.exception.RelationNotFoundException;
import gaarason.database.provider.ModelMemoryProvider;
import gaarason.database.util.ObjectUtil;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class RelationProvider<T, K> implements Relation {

    private final Record<T, K> record;

    private final RelationSubQuery relationSubQuery;

    public RelationProvider(Record<T, K> record, String columnName) {
        this.record = record;
        presetColumn(columnName);
        ModelMemoryProvider.ModelInformation<?, ?> modelInformation = ModelMemoryProvider.get(record.getModel());
        relationSubQuery = modelInformation.getRelationFieldMap().get(columnName).getRelationSubQuery();
    }

    @Override
    public void attach(Record<?, ?> targetRecord) {
        attach(targetRecord, new HashMap<>());
    }

    @Override
    public void attach(RecordList<?, ?> targetRecords) {

    }

    @Override
    public void attach(String id) {
    }

    @Override
    public void attach(Collection<String> ids) {

    }

    @Override
    public void attach(Record<?, ?> targetRecord, Map<String, String> stringStringMap) {
//        relationSubQuery.attach(record, targetRecord, stringStringMap);
    }

    @Override
    public void attach(RecordList<?, ?> targetRecords, Map<String, String> stringStringMap) {

    }

    @Override
    public void attach(String id, Map<String, String> stringStringMap) {
    }

    @Override
    public void attach(Collection<String> ids, Map<String, String> stringStringMap) {

    }

    @Override
    public boolean detach(Record<?, ?> targetRecord) {
        return false;
    }

    @Override
    public boolean detach(String id) {
        return false;
    }

    @Override
    public boolean sync(RecordList<?, ?> targetRecords) {
        return false;
    }

    @Override
    public boolean sync(Collection<String> ids) {
        return false;
    }

    @Override
    public boolean sync(RecordList<?, ?> targetRecords, Map<String, String> stringStringMap) {
        return false;
    }

    @Override
    public boolean sync(Collection<String> ids, Map<String, String> stringStringMap) {
        return false;
    }

    @Override
    public boolean toggle(RecordList<?, ?> targetRecords) {
        return false;
    }

    @Override
    public boolean toggle(Collection<String> ids) {
        return false;
    }

    @Override
    public boolean toggle(RecordList<?, ?> targetRecords, Map<String, String> stringStringMap) {
        return false;
    }

    @Override
    public boolean toggle(Collection<String> ids, Map<String, String> stringStringMap) {
        return false;
    }


    protected void presetColumn(String columnName) {
        Class<T> entityClass = record.getModel().getEntityClass();
        if (columnName.contains(".") || !ObjectUtil.checkProperties(entityClass, columnName)) {
            throw new RelationNotFoundException("实体类[" + entityClass + "]中检测属性[" + columnName + "]不通过");
        }

    }
}
