package gaarason.database.eloquent.record;

import gaarason.database.contract.eloquent.Record;
import gaarason.database.contract.eloquent.RecordList;
import gaarason.database.contract.eloquent.extra.Bind;
import gaarason.database.contract.eloquent.relation.RelationSubQuery;
import gaarason.database.exception.RelationNotFoundException;
import gaarason.database.provider.ModelShadowProvider;
import gaarason.database.support.RecordFactory;
import gaarason.database.util.ObjectUtil;

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
    public void attach(Record<?, ?> targetRecord) {
        attach(RecordFactory.newRecordList(targetRecord), new HashMap<>());
    }

    @Override
    public void attach(RecordList<?, ?> targetRecords) {
        attach(targetRecords, new HashMap<>());
    }

    @Override
    public void attach(Record<?, ?> targetRecord, Map<String, String> stringStringMap) {
        attach(RecordFactory.newRecordList(targetRecord), stringStringMap);
    }

    @Override
    public void attach(RecordList<?, ?> targetRecords, Map<String, String> stringStringMap) {
        relationSubQuery.attach(record, targetRecords, stringStringMap);
    }

    @Override
    public void attach(String id) {
        attach(Collections.singletonList(id), new HashMap<>());
    }

    @Override
    public void attach(Collection<String> ids) {
        attach(ids, new HashMap<>());

    }

    @Override
    public void attach(String id, Map<String, String> stringStringMap) {
        attach(Collections.singletonList(id), stringStringMap);
    }

    @Override
    public void attach(Collection<String> ids, Map<String, String> stringStringMap) {
        relationSubQuery.attach(record, ids, stringStringMap);
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


    protected void presetColumn(String fieldName) {
        Class<T> entityClass = record.getModel().getEntityClass();
        if (fieldName.contains(".") || !ObjectUtil.checkProperties(entityClass, fieldName)) {
            throw new RelationNotFoundException("实体类[" + entityClass + "]中检测属性[" + fieldName + "]不通过");
        }

    }
}
