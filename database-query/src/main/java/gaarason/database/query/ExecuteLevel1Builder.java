package gaarason.database.query;

import gaarason.database.contract.connection.GaarasonDataSource;
import gaarason.database.contract.eloquent.Model;
import gaarason.database.contract.eloquent.Record;
import gaarason.database.contract.eloquent.RecordList;
import gaarason.database.contract.function.GenerateSqlPartFunctionalInterface;
import gaarason.database.contract.function.RelationshipRecordWithFunctionalInterface;
import gaarason.database.contract.query.Grammar;
import gaarason.database.exception.EntityNotFoundException;
import gaarason.database.exception.SQLRuntimeException;
import gaarason.database.lang.Nullable;
import gaarason.database.util.ObjectUtils;

import java.util.*;

/**
 * 查询构造器(sql执行的部分)
 * 原始sql查询, 不会触发事件, 可以进行关联关系查询
 * @param <T>
 * @param <K>
 * @author xt
 */
public abstract class ExecuteLevel1Builder<T, K> extends BaseBuilder<T, K> {

    protected ExecuteLevel1Builder(GaarasonDataSource gaarasonDataSource, Model<T, K> model, Grammar grammar) {
        super(gaarasonDataSource, model, grammar);
    }

    /**
     * 传递有效的with信息
     * @param record 查询结果集
     */
    protected void with(Record<T, K> record){
        Map<String, Object[]> columnMap = grammar.pullWith();
        for (Map.Entry<String, Object[]> stringEntry : columnMap.entrySet()) {
            Object[] value = stringEntry.getValue();
            record.with(stringEntry.getKey(), (GenerateSqlPartFunctionalInterface<?, ?>) value[0],
                (RelationshipRecordWithFunctionalInterface) value[1]);
        }
    }

    /**
     * 传递有效的with信息
     * @param records 查询结果集
     */
    protected void with(RecordList<T, K> records){
        Map<String, Object[]> columnMap = grammar.pullWith();
        for (Map.Entry<String, Object[]> stringEntry : columnMap.entrySet()) {
            records.with(stringEntry.getKey(), (GenerateSqlPartFunctionalInterface<?, ?>) stringEntry.getValue()[0],
                (RelationshipRecordWithFunctionalInterface) stringEntry.getValue()[1]);
        }
    }

    @Nullable
    @Override
    public Record<T, K> query(String sql, @Nullable Collection<?> parameters) throws SQLRuntimeException {
        Record<T, K> record =  model.nativeQuery(sql, parameters);
        if(!ObjectUtils.isEmpty(record)){
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
