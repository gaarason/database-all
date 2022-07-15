package gaarason.database.query;

import gaarason.database.appointment.SqlType;
import gaarason.database.contract.connection.GaarasonDataSource;
import gaarason.database.contract.eloquent.Builder;
import gaarason.database.contract.eloquent.Model;
import gaarason.database.contract.eloquent.Record;
import gaarason.database.contract.eloquent.RecordList;
import gaarason.database.contract.function.ChunkFunctionalInterface;
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

    protected ExecuteLevel2Builder(GaarasonDataSource gaarasonDataSource, Model<T, K> model, Grammar grammar) {
        super(gaarasonDataSource, model, grammar);
    }

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
        return updateSql(SqlType.INSERT);
    }

    @Override
    public int update() throws SQLRuntimeException {
        return updateSql(SqlType.UPDATE);
    }

    @Override
    public int forceDelete() throws SQLRuntimeException {
        return updateSql(SqlType.DELETE);
    }

    @Override
    public K insertGetId() throws SQLRuntimeException {
        Grammar.SQLPartInfo sqlPartInfo = toSQLPartInfo(SqlType.INSERT);
        assert sqlPartInfo.getParameters() != null;
        return executeGetId(sqlPartInfo.getSqlString(), sqlPartInfo.getParameters());
    }

    @Override
    public List<K> insertGetIds() throws SQLRuntimeException {
        Grammar.SQLPartInfo sqlPartInfo = toSQLPartInfo(SqlType.INSERT);
        assert sqlPartInfo.getParameters() != null;
        return executeGetIds(sqlPartInfo.getSqlString(), sqlPartInfo.getParameters());
    }

    @Override
    public Record<T, K> firstOrFail() throws SQLRuntimeException, EntityNotFoundException {
        limit(1);
        // sql组装执行
        Grammar.SQLPartInfo sqlPartInfo = toSQLPartInfo(SqlType.SELECT);
        String sql = sqlPartInfo.getSqlString();
        Collection<Object> parameterList = sqlPartInfo.getParameters();

        return queryOrFail(sql, parameterList);
    }

    @Override
    public RecordList<T, K> get() throws SQLRuntimeException {
        // sql组装执行
        Grammar.SQLPartInfo sqlPartInfo = toSQLPartInfo(SqlType.SELECT);
        String sql = sqlPartInfo.getSqlString();
        Collection<Object> parameterList = sqlPartInfo.getParameters();
        return queryList(sql, parameterList);
    }

    @Override
    public void dealChunk(int num, ChunkFunctionalInterface<T, K> chunkFunctionalInterface) throws SQLRuntimeException {
        int offset = 0;
        boolean flag;
        do {
            Builder<T, K> cloneBuilder = clone();
            cloneBuilder.limit(offset, num);
            Grammar.SQLPartInfo sqlPartInfo = cloneBuilder.getGrammar().generateSql(SqlType.SELECT);
            String sql = sqlPartInfo.getSqlString();
            Collection<Object> parameterList = sqlPartInfo.getParameters();
            RecordList<T, K> records = queryList(sql, parameterList);
            flag = !records.isEmpty() && chunkFunctionalInterface.execute(records) && (records.size() == num);
            offset += num;
        } while (flag);
    }

    @Override
    public void dealChunk(int num, String column, ChunkFunctionalInterface<T, K> chunkFunctionalInterface)
        throws SQLRuntimeException {
        boolean flag;
        Object columnValue = null;
        do {
            Builder<T, K> cloneBuilder = clone();
            cloneBuilder.whereIgnoreNull(column, ">", columnValue)
                .firstOrderBy(builder -> builder.orderBy(column))
                .limit(num);

            Grammar.SQLPartInfo sqlPartInfo = cloneBuilder.getGrammar().generateSql(SqlType.SELECT);
            String sql = sqlPartInfo.getSqlString();
            Collection<Object> parameterList = sqlPartInfo.getParameters();
            RecordList<T, K> records = queryList(sql, parameterList);
            if (!records.isEmpty()) {
                columnValue = records.last().getMetadataMap().get(column).getValue();
            }
            flag = !records.isEmpty() && chunkFunctionalInterface.execute(records) && (records.size() == num);
        } while (flag);
    }
}
