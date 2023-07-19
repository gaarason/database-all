package gaarason.database.query;

import gaarason.database.appointment.SqlType;
import gaarason.database.contract.eloquent.Record;
import gaarason.database.contract.eloquent.RecordList;
import gaarason.database.contract.query.Grammar;
import gaarason.database.exception.ConfirmOperationException;
import gaarason.database.exception.EntityNotFoundException;
import gaarason.database.exception.SQLRuntimeException;

import java.util.Collection;
import java.util.List;

/**
 * 查询构造器(sql执行的部分)
 * @param <T>
 * @param <K>
 * @author xt
 */
public abstract class ExecuteLevel2Builder<T, K> extends ExecuteLevel1Builder<T, K> {

    /**
     * sql生成
     * @param sqlType sql 类型
     * @return SQL片段信息
     */
    Grammar.SQLPartInfo toSQLPartInfo(SqlType sqlType) {
        return grammar.generateSql(sqlType);
    }

    /**
     * 执行sql, 返回收影响的行数
     * @return 影响的行数
     * @throws SQLRuntimeException 数据库异常
     */
    int updateSql(SqlType sqlType) throws SQLRuntimeException {
        if (sqlType != SqlType.INSERT && grammar.isEmpty(Grammar.SQLPartType.WHERE)) {
            throw new ConfirmOperationException(
                "You made a risky operation without where conditions, use where(1) for sure");
        }
        // sql组装执行
        Grammar.SQLPartInfo sqlPartInfo = toSQLPartInfo(sqlType);
        String sql = sqlPartInfo.getSqlString();
        Collection<Object> parameters = sqlPartInfo.getParameters();
        return execute(sql, parameters);
    }

    @Override
    public int insert() throws SQLRuntimeException {
        // 事件
        model.creating(this);
        int rows = updateSql(SqlType.INSERT);
        model.created(rows);
        return rows;
    }

    @Override
    public int update() throws SQLRuntimeException {
        // 事件
        model.updating(this);
        int rows = updateSql(SqlType.UPDATE);
        model.updated(rows);
        return rows;
    }

    @Override
    public int restore() throws SQLRuntimeException {
        // 事件
        model.restoring(this);
        int rows = model.restore(this);
        model.restored(rows);
        return rows;
    }

    @Override
    public int delete() throws SQLRuntimeException {
        // 事件
        model.deleting(this);
        int rows = model.delete(this);
        model.deleted(rows);
        return rows;
    }

    @Override
    public int forceDelete() throws SQLRuntimeException {
        // 事件
        model.deleting(this);
        int rows = updateSql(SqlType.DELETE);
        model.deleted(rows);
        return rows;
    }

    @Override
    public K insertGetId() throws SQLRuntimeException {
        // 事件
        model.creating(this);
        Grammar.SQLPartInfo sqlPartInfo = toSQLPartInfo(SqlType.INSERT);
        K id = executeGetId(sqlPartInfo.getSqlString(), sqlPartInfo.getParameters());
        if (id != null) {
            model.created(id);
        }
        return id;
    }

    @Override
    public List<K> insertGetIds() throws SQLRuntimeException {
        // 事件
        model.creating(this);
        Grammar.SQLPartInfo sqlPartInfo = toSQLPartInfo(SqlType.INSERT);
        List<K> ids = executeGetIds(sqlPartInfo.getSqlString(), sqlPartInfo.getParameters());
        model.created(ids);
        return ids;
    }

    @Override
    public Record<T, K> firstOrFail() throws SQLRuntimeException, EntityNotFoundException {
        limit(1);
        // 事件
        model.retrieving(this);

        Grammar.SQLPartInfo sqlPartInfo = toSQLPartInfo(SqlType.SELECT);
        String sql = sqlPartInfo.getSqlString();
        Collection<Object> parameterList = sqlPartInfo.getParameters();
        Record<T, K> record = queryOrFail(sql, parameterList);

        // 事件
        model.retrieved(record);
        return record;
    }

    @Override
    public RecordList<T, K> get() throws SQLRuntimeException {
        // 事件
        model.retrieving(this);

        // sql组装执行
        Grammar.SQLPartInfo sqlPartInfo = toSQLPartInfo(SqlType.SELECT);
        String sql = sqlPartInfo.getSqlString();
        Collection<Object> parameterList = sqlPartInfo.getParameters();
        RecordList<T, K> records = queryList(sql, parameterList);

        // 事件
        model.retrieved(records);
        return records;
    }

}
