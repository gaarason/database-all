package gaarason.database.eloquent.record.extra;

import gaarason.database.contracts.function.GenerateSqlPart;
import gaarason.database.contracts.record.extra.Relation;
import gaarason.database.eloquent.Record;
import gaarason.database.eloquent.RecordList;
import gaarason.database.exception.RelationNotFoundException;
import gaarason.database.utils.ObjectUtil;

import java.util.Collection;

public class RelationProvider<T, K> implements Relation {

    private final Record<T, K> record;

    private final String column;

    public RelationProvider(Record<T, K> record, String column) {
        this.record = record;
        checkColumn(column);
        this.column = column;
    }

    @Override
    public boolean attach(Record<?, ?> targetRecord) {
        // 获取关系类型

        // 获取关系键的值
        ObjectUtil.checkProperties(record.getModel().getEntityClass(), column);


        return true;
    }

    @Override
    public boolean detach(Record<?, ?> targetRecord) {
        return false;
    }

    @Override
    public boolean sync(RecordList<?, ?> targetRecords) {
        return false;
    }

    @Override
    public boolean syncWithoutDetaching(RecordList<?, ?> targetRecords) {
        return false;
    }

    @Override
    public boolean toggle(RecordList<?, ?> targetRecords) {
        return false;
    }

    @Override
    public boolean attach(String id) {
        return false;
    }

    @Override
    public boolean detach(String id) {
        return false;
    }

    @Override
    public boolean sync(Collection<String> ids) {
        return false;
    }

    @Override
    public boolean syncWithoutDetaching(Collection<String> ids) {
        return false;
    }

    @Override
    public boolean toggle(Collection<String> ids) {
        return false;
    }

    @Override
    public boolean attach(Record<?, ?> targetRecord, GenerateSqlPart closure) {
        return false;
    }

    @Override
    public boolean detach(Record<?, ?> targetRecord, GenerateSqlPart closure) {
        return false;
    }

    @Override
    public boolean sync(RecordList<?, ?> targetRecords, GenerateSqlPart closure) {
        return false;
    }

    @Override
    public boolean syncWithoutDetaching(RecordList<?, ?> targetRecords, GenerateSqlPart closure) {
        return false;
    }

    @Override
    public boolean toggle(RecordList<?, ?> targetRecords, GenerateSqlPart closure) {
        return false;
    }

    @Override
    public boolean attach(String id, GenerateSqlPart closure) {
        return false;
    }

    @Override
    public boolean detach(String id, GenerateSqlPart closure) {
        return false;
    }

    @Override
    public boolean sync(Collection<String> ids, GenerateSqlPart closure) {
        return false;
    }

    @Override
    public boolean syncWithoutDetaching(Collection<String> ids, GenerateSqlPart closure) {
        return false;
    }

    @Override
    public boolean toggle(Collection<String> ids, GenerateSqlPart closure) {
        return false;
    }


    private void checkColumn(String column) {
        Class<T> entityClass = record.getModel().getEntityClass();
        if (column.contains(".") || !ObjectUtil.checkProperties(entityClass, column)) {
            throw new RelationNotFoundException("实体类[" + entityClass + "]中检测属性[" + column + "]不通过");
        }

    }
}
