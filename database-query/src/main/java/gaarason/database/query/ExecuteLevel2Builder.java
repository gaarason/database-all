package gaarason.database.query;

import gaarason.database.appointment.SqlType;
import gaarason.database.contract.eloquent.Builder;
import gaarason.database.contract.eloquent.Record;
import gaarason.database.contract.eloquent.RecordList;
import gaarason.database.contract.query.Grammar;
import gaarason.database.exception.ConfirmOperationException;
import gaarason.database.exception.EntityNotFoundException;
import gaarason.database.exception.SQLRuntimeException;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * 查询构造器(sql执行的部分)
 * @param <T>
 * @param <K>
 * @author xt
 */
abstract class ExecuteLevel2Builder<B extends Builder<B, T, K>, T, K>  extends ExecuteLevel1Builder<B, T, K> {

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
        if ((sqlType != SqlType.INSERT && sqlType != SqlType.REPLACE) && grammar.isEmpty(Grammar.SQLPartType.WHERE)) {
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
    public int replace() throws SQLRuntimeException {
        // 暂无事件
        return updateSql(SqlType.REPLACE);
    }

    @Override
    public int insert() throws SQLRuntimeException {
        // 事件
        model.eventQueryCreating(this);
        int rows = updateSql(SqlType.INSERT);
        model.eventQueryCreated(rows);
        return rows;
    }

    @Override
    public int upsert(String... columns) throws SQLRuntimeException {
        return upsert(Arrays.asList(columns));
    }

    @Override
    public int upsert(Collection<String> columns) throws SQLRuntimeException {
        // 暂无事件
        // sql 语句拼接
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("ON DUPLICATE KEY UPDATE ");
        Iterator<String> iterator = columns.iterator();
        while (iterator.hasNext()) {
            // 列名
            String column = iterator.next();
            // `列名`
            String backQuoteColumn = supportBackQuote(column);
            // `列名`=VALUES(`列名`)
            sqlBuilder.append(backQuoteColumn)
                    .append("=VALUES")
                    .append(supportBracket(backQuoteColumn));
            if (iterator.hasNext()) {
                sqlBuilder.append(", ");
            }
        }
        // 加入 末端
        lastRaw(sqlBuilder.toString());
        // 执行
        return updateSql(SqlType.INSERT);
    }

    @Override
    public int update() throws SQLRuntimeException {
        // 事件
        model.eventQueryUpdating(this);
        int rows = updateSql(SqlType.UPDATE);
        model.eventQueryUpdated(rows);
        return rows;
    }

    @Override
    public int restore() throws SQLRuntimeException {
        // 事件
        model.eventQueryRestoring(this);
        int rows = model.restore(this);
        model.eventQueryRestored(rows);
        return rows;
    }

    @Override
    public int delete() throws SQLRuntimeException {
        // 事件
        model.eventQueryDeleting(this);
        int rows = model.delete(this);
        model.eventQueryDeleted(rows);
        return rows;
    }

    @Override
    public int forceDelete() throws SQLRuntimeException {
        // 事件
        model.eventQueryDeleting(this);
        int rows = updateSql(SqlType.DELETE);
        model.eventQueryDeleted(rows);
        return rows;
    }

    @Override
    public K insertGetId() throws SQLRuntimeException {
        // 事件
        model.eventQueryCreating(this);
        Grammar.SQLPartInfo sqlPartInfo = toSQLPartInfo(SqlType.INSERT);
        K id = executeGetId(sqlPartInfo.getSqlString(), sqlPartInfo.getParameters());
        if (id != null) {
            model.eventQueryCreated(id);
        }
        return id;
    }

    @Override
    public List<K> insertGetIds() throws SQLRuntimeException {
        // 事件
        model.eventQueryCreating(this);
        Grammar.SQLPartInfo sqlPartInfo = toSQLPartInfo(SqlType.INSERT);
        List<K> ids = executeGetIds(sqlPartInfo.getSqlString(), sqlPartInfo.getParameters());
        model.eventQueryCreated(ids);
        return ids;
    }

    @Override
    public Record<T, K> firstOrFail() throws SQLRuntimeException, EntityNotFoundException {
        limit(1);
        // 事件
        model.eventQueryRetrieving(this);

        Grammar.SQLPartInfo sqlPartInfo = toSQLPartInfo(SqlType.SELECT);
        String sql = sqlPartInfo.getSqlString();
        Collection<Object> parameterList = sqlPartInfo.getParameters();
        Record<T, K> record = queryOrFail(sql, parameterList);

        // 事件
        model.eventQueryRetrieved(record);
        return record;
    }

    @Override
    public RecordList<T, K> get() throws SQLRuntimeException {
        // 事件
        model.eventQueryRetrieving(this);

        // sql组装执行
        Grammar.SQLPartInfo sqlPartInfo = toSQLPartInfo(SqlType.SELECT);
        String sql = sqlPartInfo.getSqlString();
        Collection<Object> parameterList = sqlPartInfo.getParameters();
        RecordList<T, K> records = queryList(sql, parameterList);

        // 事件
        model.eventQueryRetrieved(records);
        return records;
    }

}
