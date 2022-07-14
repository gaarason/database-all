package gaarason.database.eloquent.record;

import gaarason.database.contract.eloquent.Record;
import gaarason.database.contract.eloquent.RecordList;
import gaarason.database.contract.eloquent.extra.Bind;
import gaarason.database.contract.eloquent.relation.RelationSubQuery;
import gaarason.database.exception.RelationNotFoundException;
import gaarason.database.provider.ModelInfo;
import gaarason.database.provider.ModelShadowProvider;
import gaarason.database.provider.RelationFieldInfo;
import gaarason.database.support.ModelMember;
import gaarason.database.support.RecordFactory;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Bind的实现
 * @author xt
 */
public class BindBean<T extends Serializable, K extends Serializable> implements Bind {

    private final Record<T, K> tkRecord;

    private final RelationSubQuery relationSubQuery;

    public BindBean(Record<T, K> tkRecord, String fieldName) {
        this.tkRecord = tkRecord;
        // 模型信息
        ModelMember<T, K> modelMember = tkRecord.getModel()
            .getGaarasonDataSource()
            .getContainer()
            .getBean(ModelShadowProvider.class)
            .get(tkRecord.getModel());
        // 关系信息
        relationSubQuery = modelMember.getEntityMember().getFieldRelationMemberByFieldName(fieldName).getRelationSubQuery();
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
    public int attach(Record<?, ?> targetRecord, Map<String, Object> relationDataMap) {
        return attach(RecordFactory.newRecordList(targetRecord), relationDataMap);
    }

    @Override
    public int attach(RecordList<?, ?> targetRecords, Map<String, Object> relationDataMap) {
        return relationSubQuery.attach(tkRecord, targetRecords, relationDataMap);
    }

    @Override
    public int attach(Object id) {
        return attach(Collections.singletonList(id), new HashMap<>());
    }

    @Override
    public int attach(Collection<Object> ids) {
        return attach(ids, new HashMap<>());

    }

    @Override
    public int attach(Object id, Map<String, Object> relationDataMap) {
        return attach(Collections.singletonList(id), relationDataMap);
    }

    @Override
    public int attach(Collection<Object> ids, Map<String, Object> relationDataMap) {
        return relationSubQuery.attach(tkRecord, ids, relationDataMap);
    }

    @Override
    public int detach() {
        return relationSubQuery.detach(tkRecord);
    }

    @Override
    public int detach(Record<?, ?> targetRecord) {
        return detach(RecordFactory.newRecordList(targetRecord));
    }

    @Override
    public int detach(RecordList<?, ?> targetRecords) {
        return relationSubQuery.detach(tkRecord, targetRecords);
    }

    @Override
    public int detach(Object id) {
        return detach(Collections.singletonList(id));
    }

    @Override
    public int detach(Collection<Object> ids) {
        return relationSubQuery.detach(tkRecord, ids);
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
    public int sync(Record<?, ?> targetRecord, Map<String, Object> relationDataMap) {
        return sync(RecordFactory.newRecordList(targetRecord), relationDataMap);
    }

    @Override
    public int sync(RecordList<?, ?> targetRecords, Map<String, Object> relationDataMap) {
        return relationSubQuery.sync(tkRecord, targetRecords, relationDataMap);
    }

    @Override
    public int sync(Object id) {
        return sync(Collections.singletonList(id), new HashMap<>());
    }

    @Override
    public int sync(Collection<Object> ids) {
        return sync(ids, new HashMap<>());
    }

    @Override
    public int sync(Object id, Map<String, Object> relationDataMap) {
        return sync(Collections.singletonList(id), relationDataMap);
    }

    @Override
    public int sync(Collection<Object> ids, Map<String, Object> relationDataMap) {
        return relationSubQuery.sync(tkRecord, ids, relationDataMap);
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
    public int toggle(Record<?, ?> targetRecord, Map<String, Object> relationDataMap) {
        return toggle(RecordFactory.newRecordList(targetRecord), relationDataMap);
    }

    @Override
    public int toggle(RecordList<?, ?> targetRecords, Map<String, Object> relationDataMap) {
        return relationSubQuery.toggle(tkRecord, targetRecords, relationDataMap);
    }

    @Override
    public int toggle(Object id) {
        return toggle(Collections.singletonList(id), new HashMap<>());
    }

    @Override
    public int toggle(Collection<Object> ids) {
        return toggle(ids, new HashMap<>());
    }

    @Override
    public int toggle(Object id, Map<String, Object> relationDataMap) {
        return toggle(Collections.singletonList(id), relationDataMap);
    }

    @Override
    public int toggle(Collection<Object> ids, Map<String, Object> relationDataMap) {
        return relationSubQuery.toggle(tkRecord, ids, relationDataMap);
    }
}
