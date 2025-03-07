package gaarason.database.query;

import gaarason.database.contract.eloquent.Builder;
import gaarason.database.contract.eloquent.Record;
import gaarason.database.contract.eloquent.RecordList;
import gaarason.database.exception.EntityNotFoundException;
import gaarason.database.exception.SQLRuntimeException;
import gaarason.database.lang.Nullable;
import gaarason.database.util.ObjectUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 查询构造器(sql执行的部分)
 * 原始sql查询, 不会触发事件, 可以进行关联关系查询
 * @param <T>
 * @param <K>
 * @author xt
 */
abstract class ExecuteLevel1Builder<B extends Builder<B, T, K>, T, K> extends BaseBuilder<B, T, K> {

    /**
     * 传递有效的with信息
     * @param record 查询结果集
     */
    protected void with(Record<T, K> record) {
        Map<String, Record.Relation> relationMap = grammar.pullRelation();
        relationMap.forEach((k, v) -> {
            if (!v.relationOperation) {
                // 关联关系属性
                record.with(k, v.customBuilder, v.recordWrapper);
            } else {
                // 关联关系操作属性
                record.getRelationMap().put(k, v);
            }
        });
    }

    /**
     * 传递有效的with信息
     * @param records 查询结果集
     */
    protected void with(RecordList<T, K> records) {
        Map<String, Record.Relation> relationMap = grammar.pullRelation();
        relationMap.forEach((k, v) -> {
            if (!v.relationOperation) {
                // 关联关系属性
                records.with(k, v.customBuilder, v.recordWrapper);
            } else {
                // 关联关系操作属性
                for (Record<T, K> record : records) {
                    record.getRelationMap().put(k, v);
                }
            }
        });
    }

    @Nullable
    @Override
    public Record<T, K> query(String sql, @Nullable Collection<?> parameters) throws SQLRuntimeException {
        Record<T, K> record = model.nativeQuery(sql, parameters);
        if (!ObjectUtils.isEmpty(record)) {
            with(record);
        }
        return record;
    }

    @Override
    public Record<T, K> queryOrFail(String sql, @Nullable Collection<?> parameters)
        throws SQLRuntimeException, EntityNotFoundException {
        Record<T, K> record = model.nativeQueryOrFail(sql, parameters);
        with(record);
        return record;
    }

    @Override
    public RecordList<T, K> queryList(String sql, @Nullable Collection<?> parameters) throws SQLRuntimeException {
        RecordList<T, K> records = model.nativeQueryList(sql, parameters);
        with(records);
        return records;
    }

    @Override
    public int execute(String sql, @Nullable Collection<?> parameters) throws SQLRuntimeException {
        return model.nativeExecute(sql, parameters);
    }

    @Override
    public List<K> executeGetIds(String sql, @Nullable Collection<?> parameters) throws SQLRuntimeException {
        return model.nativeExecuteGetIds(sql, parameters);
    }

    @Override
    @Nullable
    public K executeGetId(String sql, @Nullable Collection<?> parameters) throws SQLRuntimeException {
        return model.nativeExecuteGetId(sql, parameters);
    }

}
