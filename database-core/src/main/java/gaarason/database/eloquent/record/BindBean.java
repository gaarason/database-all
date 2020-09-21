package gaarason.database.eloquent.record;

import gaarason.database.contract.eloquent.Record;
import gaarason.database.contract.eloquent.RecordList;
import gaarason.database.contract.eloquent.extra.Bind;
import gaarason.database.contract.eloquent.relation.RelationSubQuery;
import gaarason.database.exception.RelationNotFoundException;
import gaarason.database.provider.ModelShadowProvider;
import gaarason.database.support.RecordFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class BindBean<T, K> implements Bind {

    private final Record<T, K> record;

    private final RelationSubQuery relationSubQuery;

    public BindBean(Record<T, K> record, String fieldName) {
        this.record = record;
        // 模型信息
        ModelShadowProvider.ModelInfo<?, ?> modelInfo = ModelShadowProvider.get(record.getModel());
        // 关系信息
        ModelShadowProvider.RelationFieldInfo relationFieldInfo = modelInfo.getRelationFieldMap().get(fieldName);
        if (relationFieldInfo == null) {
            throw new RelationNotFoundException(
                    "No associations were found for property[" + fieldName + "] in the entity[" + modelInfo.getEntityClass() + "].");
        }
        relationSubQuery = relationFieldInfo.getRelationSubQuery();
    }

    @Override
    public int attach(Record<?, ?> targetRecord) {
        return attach(RecordFactory.newRecordList(targetRecord), new HashMap<>());
    }

    @Override
    public int attach(RecordList<?, ?> targetRecords) {
        return attach(targetRecords, new HashMap<>());
    }

    @Override
    public int attach(Record<?, ?> targetRecord, Map<String, String> stringStringMap) {
        return attach(RecordFactory.newRecordList(targetRecord), stringStringMap);
    }

    @Override
    public int attach(RecordList<?, ?> targetRecords, Map<String, String> stringStringMap) {
        return relationSubQuery.attach(record, targetRecords, stringStringMap);
    }

    @Override
    public int attach(String id) {
        return attach(Collections.singletonList(id), new HashMap<>());
    }

    @Override
    public int attach(Collection<String> ids) {
        return attach(ids, new HashMap<>());

    }

    @Override
    public int attach(String id, Map<String, String> stringStringMap) {
        return attach(Collections.singletonList(id), stringStringMap);
    }

    @Override
    public int attach(Collection<String> ids, Map<String, String> stringStringMap) {
        return relationSubQuery.attach(record, ids, stringStringMap);
    }

    @Override
    public int detach() {
        return relationSubQuery.detach(record);
    }

    @Override
    public int detach(Record<?, ?> targetRecord) {
        return detach(RecordFactory.newRecordList(targetRecord));
    }

    @Override
    public int detach(RecordList<?, ?> targetRecords) {
        return relationSubQuery.detach(record, targetRecords);
    }

    @Override
    public int detach(String id) {
        return detach(Collections.singletonList(id));
    }

    @Override
    public int detach(Collection<String> ids) {
        return relationSubQuery.detach(record, ids);
    }

    @Override
    public int sync(Record<?, ?> targetRecord) {
        return sync(RecordFactory.newRecordList(targetRecord), new HashMap<>());

    }

    @Override
    public int sync(RecordList<?, ?> targetRecords) {
        return sync(targetRecords, new HashMap<>());
    }

    @Override
    public int sync(Record<?, ?> targetRecord, Map<String, String> stringStringMap) {
        return sync(RecordFactory.newRecordList(targetRecord), stringStringMap);
    }

    @Override
    public int sync(RecordList<?, ?> targetRecords, Map<String, String> stringStringMap) {
        return relationSubQuery.sync(record, targetRecords, stringStringMap);
    }

    @Override
    public int sync(String id) {
        return sync(Collections.singletonList(id), new HashMap<>());
    }

    @Override
    public int sync(Collection<String> ids) {
        return sync(ids, new HashMap<>());
    }

    @Override
    public int sync(String id, Map<String, String> stringStringMap) {
        return sync(Collections.singletonList(id), stringStringMap);
    }

    @Override
    public int sync(Collection<String> ids, Map<String, String> stringStringMap) {
        return relationSubQuery.sync(record, ids, stringStringMap);
    }

    @Override
    public int toggle(Record<?, ?> targetRecord) {
        return toggle(RecordFactory.newRecordList(targetRecord), new HashMap<>());
    }

    @Override
    public int toggle(RecordList<?, ?> targetRecords) {
        return toggle(targetRecords, new HashMap<>());
    }

    @Override
    public int toggle(Record<?, ?> targetRecord, Map<String, String> stringStringMap) {
        return toggle(RecordFactory.newRecordList(targetRecord), stringStringMap);
    }

    @Override
    public int toggle(RecordList<?, ?> targetRecords, Map<String, String> stringStringMap) {
        return relationSubQuery.toggle(record, targetRecords, stringStringMap);
    }

    @Override
    public int toggle(String id) {
        return toggle(Collections.singletonList(id), new HashMap<>());
    }

    @Override
    public int toggle(Collection<String> ids) {
        return toggle(ids, new HashMap<>());
    }

    @Override
    public int toggle(String id, Map<String, String> stringStringMap) {
        return toggle(Collections.singletonList(id), stringStringMap);
    }

    @Override
    public int toggle(Collection<String> ids, Map<String, String> stringStringMap) {
        return relationSubQuery.toggle(record, ids, stringStringMap);
    }
}
