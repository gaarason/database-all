package gaarason.database.query;

import gaarason.database.appointment.ValueWrapper;
import gaarason.database.contract.connection.GaarasonDataSource;
import gaarason.database.contract.eloquent.Model;
import gaarason.database.contract.eloquent.Record;
import gaarason.database.contract.eloquent.RecordList;
import gaarason.database.contract.function.ExecSqlWithinConnectionFunctionalInterface;
import gaarason.database.contract.function.GenerateSqlPartFunctionalInterface;
import gaarason.database.contract.function.RelationshipRecordWithFunctionalInterface;
import gaarason.database.contract.query.Grammar;
import gaarason.database.exception.EntityNotFoundException;
import gaarason.database.exception.PrimaryKeyTypeNotSupportException;
import gaarason.database.exception.SQLRuntimeException;
import gaarason.database.lang.Nullable;
import gaarason.database.support.RecordFactory;
import gaarason.database.util.ObjectUtils;

import java.io.Serializable;
import java.sql.*;
import java.util.*;

/**
 * 查询构造器(sql执行的部分)
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
            record.with(stringEntry.getKey(), (GenerateSqlPartFunctionalInterface) value[0],
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

    @Override
    public Record<T, K> queryOrFail(String sql, @Nullable Collection<?> parameters)
        throws SQLRuntimeException, EntityNotFoundException {
        Record<T, K> record = doSomethingInConnection(preparedStatement -> {
            ResultSet resultSet = preparedStatement.executeQuery();
            return RecordFactory.newRecord(model, resultSet, sql);
        }, sql, parameters, false);
        with(record);
        return record;
    }

    @Override
    public RecordList<T, K> queryList(String sql, @Nullable Collection<?> parameters) throws SQLRuntimeException {
        RecordList<T, K> records = doSomethingInConnection(preparedStatement -> {
            ResultSet resultSet = preparedStatement.executeQuery();
            return RecordFactory.newRecordList(model, resultSet, sql);
        }, sql, parameters, false);
        with(records);
        return records;
    }

    @Override
    public int execute(String sql, @Nullable Collection<?> parameters) throws SQLRuntimeException {
        return doSomethingInConnection(PreparedStatement::executeUpdate, sql, parameters, true);
    }

    @Override
    public List<K> executeGetIds(String sql, @Nullable Collection<?> parameters) throws SQLRuntimeException {
        return doSomethingInConnection(preparedStatement -> {
            List<K> ids = new ArrayList<>();
            // 执行
            preparedStatement.executeUpdate();
            // 执行成功
            ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
            while (generatedKeys.next()) {
                ids.add(getGeneratedKeys(generatedKeys));
            }
            generatedKeys.close();
            return ids;
        }, sql, parameters, true);
    }


    @Override
    @Nullable
    public K executeGetId(String sql, @Nullable Collection<?> parameters) throws SQLRuntimeException {
        return doSomethingInConnection(preparedStatement -> {
            // 执行
            int affectedRows = preparedStatement.executeUpdate();
            // 执行成功
            if (affectedRows > 0) {
                // 获取键
                ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
                generatedKeys.next();
                K key = getGeneratedKeys(generatedKeys); //得到第一个键值
                generatedKeys.close();
                return key;
            }
            return null;
        }, sql, parameters, true);
    }

    /**
     * 在连接中执行
     * @param closure 闭包
     * @param sql 带占位符的sql
     * @param parameters sql的参数
     * @param isWrite 是否写(主)链接
     * @param <U> 响应类型
     * @return 响应
     * @throws SQLRuntimeException 数据库异常
     */
    protected <U> U doSomethingInConnection(ExecSqlWithinConnectionFunctionalInterface<U> closure, String sql,
        @Nullable Collection<?> parameters, boolean isWrite) throws SQLRuntimeException {
        Collection<?> localParameters = parameters == null ? Collections.EMPTY_LIST : parameters;
        // 获取连接
        Connection connection = gaarasonDataSource.getLocalConnection(isWrite);
        try {
            // 参数准备
            PreparedStatement preparedStatement = executeSql(connection, sql, localParameters);
            // 执行
            return closure.execute(preparedStatement);
        } catch (EntityNotFoundException e) {
            throw new EntityNotFoundException(String.format(sql.replace(" ? ", "\"%s\""), localParameters.toArray()));
        } catch (Throwable e) {
            throw new SQLRuntimeException(sql, localParameters, e.getMessage(),
                gaarasonDataSource.getQueryBuilder().getValueSymbol(), e);
        } finally {
            // 关闭连接
            gaarasonDataSource.localConnectionClose(connection);
        }
    }

    /**
     * 获取主键
     * @param generatedKeys 数据集
     * @return 主键
     * @throws SQLException 数据库异常
     * @throws PrimaryKeyTypeNotSupportException 不支持的主键类型
     */
    private K getGeneratedKeys(ResultSet generatedKeys) throws SQLException, PrimaryKeyTypeNotSupportException {
        Class<K> primaryKeyClass = model.getPrimaryKeyClass();
        if (Byte.class.equals(primaryKeyClass) || byte.class.equals(primaryKeyClass)) {
            return ObjectUtils.typeCast(generatedKeys.getByte(1));
        } else if (Integer.class.equals(primaryKeyClass) || int.class.equals(primaryKeyClass)) {
            return ObjectUtils.typeCast(generatedKeys.getInt(1));
        } else if (Long.class.equals(primaryKeyClass) || long.class.equals(primaryKeyClass)) {
            return ObjectUtils.typeCast(generatedKeys.getLong(1));
        } else if (String.class.equals(primaryKeyClass) || Object.class.equals(primaryKeyClass) ||
            Serializable.class.equals(primaryKeyClass)) {
            return ObjectUtils.typeCast(generatedKeys.getString(1));
        }
        throw new PrimaryKeyTypeNotSupportException(
            "Primary key type [" + primaryKeyClass + "] not support get " + "generated keys yet.");
    }

    /**
     * 执行sql
     * @param connection 数据库连接
     * @param sql 查询语句
     * @param parameterList 参数绑定
     * @return 预执行对象
     * @throws SQLException sql错误
     */
    protected PreparedStatement executeSql(Connection connection, String sql, Collection<?> parameterList)
        throws SQLException {
        // 日志记录
        model.log(sql, parameterList);
        // 预执行 ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY
        PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        // 参数绑定
        int i = 1;
        for (Object parameter : parameterList) {
            setParameter(preparedStatement, i++, parameter);
        }
        // 返回预执行对象
        return preparedStatement;
    }

    /**
     * 单个参数绑定
     * @param preparedStatement 预执行对象
     * @param index 参数索引
     * @param parameter 参数对象
     */
    protected void setParameter(PreparedStatement preparedStatement, int index, Object parameter) throws SQLException {
        if (parameter instanceof ValueWrapper) {
            // 精确类型
            preparedStatement.setObject(index, ((ValueWrapper) parameter).getValue(),
                ((ValueWrapper) parameter).getType());
        } else {
            // 全凭运气
            preparedStatement.setObject(index, parameter);
        }
    }

}
