package gaarason.database.query;

import gaarason.database.connections.ProxyDataSource;
import gaarason.database.contracts.Grammar;
import gaarason.database.contracts.builder.OrderBy;
import gaarason.database.contracts.builder.*;
import gaarason.database.contracts.function.Chunk;
import gaarason.database.contracts.function.GenerateSqlPart;
import gaarason.database.core.lang.Nullable;
import gaarason.database.eloquent.*;
import gaarason.database.exception.*;
import gaarason.database.support.RecordFactory;
import gaarason.database.utils.ExceptionUtil;
import gaarason.database.utils.FormatUtil;

import java.lang.management.ManagementFactory;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

abstract public class Builder<T> implements Cloneable, Where<T>, Having<T>, Union<T>, Support<T>, From<T>, Execute<T>,
    Select<T>, OrderBy<T>, Limit<T>, Group<T>, Value<T>, Data<T>, Transaction<T>, Aggregates<T>, Paginator<T>,
    Lock<T>, Native<T>, Join<T>, Ability<T> {

    /**
     * 数据实体类
     */
    Class<T> entityClass;

    /**
     * 数据库连接
     */
    private ProxyDataSource dataSource;

    /**
     * 避免线程锁的connection缓存
     */
    private static Map<String, Connection> localThreadConnectionList = new HashMap<>();

    /**
     * sql生成器
     */
    Grammar grammar;

    /**
     * 数据模型
     */
    protected Model<T> model;

    public Builder(ProxyDataSource dataSource, Model<T> model, Class<T> entityClass) {
        this.dataSource = dataSource;
        this.model = model;
        this.entityClass = entityClass;
        grammar = grammarFactory();
    }

    /**
     * 得到一个全新的查询构造器
     * @return 查询构造器
     */
    private Builder<T> getNewSelf() {
        return model.newQuery();
    }

    /**
     * 执行闭包生成sqlPart
     * @param closure 闭包
     * @return sqlPart eg:(`id`="3" and `age` between "12" and "19")
     */
    String generateSqlPart(GenerateSqlPart<T> closure) {
        return generateSql(closure, false);
    }

    /**
     * 执行闭包生成完整sql
     * @param closure 闭包
     * @return sqlPart eg:(select * from `student` where `id`="3" and `age` between "12" and "19")
     */
    String generateSql(GenerateSqlPart<T> closure) {
        return generateSql(closure, true);
    }

    /**
     * @return 数据库语句组装对象
     */
    abstract Grammar grammarFactory();

    @Override
    public Paginate<T> paginate(int currentPage, int perPage)
        throws SQLRuntimeException, CloneNotSupportedRuntimeException {
        Long    count = clone().count("*");
        List<T> list  = limit((currentPage - 1) * perPage, perPage).get().toObjectList();
        return new Paginate<>(list, currentPage, perPage, count.intValue());
    }

    @Override
    public Builder clone() throws CloneNotSupportedRuntimeException {
        try {
            Builder builder = (Builder) super.clone();
            builder.grammar = grammar.clone();
            return builder;
        } catch (CloneNotSupportedException e) {
            throw new CloneNotSupportedRuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public Paginate<T> simplePaginate(int currentPage, int perPage) throws SQLRuntimeException {
        List<T> list = limit((currentPage - 1) * perPage, perPage).get().toObjectList();
        return new Paginate<>(list, currentPage, perPage);
    }

    @Override
    public void begin() throws SQLRuntimeException, NestedTransactionException {
        if (dataSource.isInTransaction()) {
            throw new NestedTransactionException();
        }
        try {
            dataSource.setInTransaction();
            Connection connection = dataSource.getConnection();
            connection.setAutoCommit(false);
            setLocalThreadConnection(connection);
        } catch (SQLException e) {
            throw new SQLRuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public void commit() throws SQLRuntimeException {
        try {
            getLocalThreadConnection().commit();
            getLocalThreadConnection().close();
        } catch (SQLException e) {
            throw new SQLRuntimeException(e.getMessage(), e);
        } finally {
            removeLocalThreadConnection();
            dataSource.setOutTransaction();
        }
    }

    @Override
    public void rollBack() throws SQLRuntimeException {
        try {
            getLocalThreadConnection().rollback();
            getLocalThreadConnection().close();
        } catch (SQLException e) {
            throw new SQLRuntimeException(e.getMessage(), e);
        } finally {
            removeLocalThreadConnection();
            dataSource.setOutTransaction();
        }
    }

    @Override
    public boolean inTransaction() {
        return dataSource.isInTransaction();
    }

    @Override
    public boolean transaction(Runnable runnable, int maxAttempts) {
        for (int currentAttempt = 1; currentAttempt <= maxAttempts; currentAttempt++) {
            begin();
            try {
                runnable.run();
                commit();
                return true;
            } catch (Throwable e) {
                rollBack();
                if (!ExceptionUtil.causedByDeadlock(e)) {
                    throw e;
                }
            }
        }
        return false;
    }

    /**
     * 恢复软删除模型
     * @return 受影响的行数
     * @throws SQLRuntimeException sql异常
     */
    @Override
    public int restore() throws SQLRuntimeException {
        return model.restore(this);
    }

    /**
     * 删除
     * @return 受影响的行数
     * @throws SQLRuntimeException sql异常
     */
    @Override
    public int delete() throws SQLRuntimeException {
        return model.delete(this);
    }

    /**
     * 绝对真删除
     * @return 受影响的行数
     * @throws SQLRuntimeException sql异常
     */
    @Override
    public int forceDelete() throws SQLRuntimeException {
        return updateSql(SqlType.DELETE);
    }

    @Override
    public Record<T> queryOrFail(String sql, Collection<String> parameters)
        throws SQLRuntimeException, EntityNotFoundException {
        Connection connection = theConnection(false);
        try {
            ResultSet resultSet = executeSql(connection, sql, parameters).executeQuery();
            return RecordFactory.newRecord(entityClass, model, resultSet);
        } catch (SQLException e) {
            throw new SQLRuntimeException(e.getMessage(), e);
        } finally {
            if (!inTransaction()) {
                connectionClose(connection);
            }
        }
    }

    @Nullable
    @Override
    public Record<T> query(String sql, Collection<String> parameters) throws SQLRuntimeException {
        try {
            return queryOrFail(sql, parameters);
        } catch (EntityNotFoundException e) {
            return null;
        }
    }

    @Override
    public RecordList<T> queryList(String sql, Collection<String> parameters) throws SQLRuntimeException {
        Connection connection = theConnection(false);
        try {
            ResultSet resultSet = executeSql(connection, sql, parameters).executeQuery();
            return RecordFactory.newRecordList(entityClass, model, resultSet);
        } catch (SQLException e) {
            throw new SQLRuntimeException(sql, parameters, e.getMessage(), e);
        } finally {
            if (!inTransaction()) {
                connectionClose(connection);
            }
        }
    }

    @Override
    public int execute(String sql, Collection<String> parameters) throws SQLRuntimeException {
        Connection connection = theConnection(true);
        try {
            return executeSql(connection, sql, parameters).executeUpdate();
        } catch (SQLException e) {
            throw new SQLRuntimeException(sql, parameters, e.getMessage(), e);
        } finally {
            if (!inTransaction()) {
                connectionClose(connection);
            }
        }
    }

    /**
     * 执行sql, 处理jdbc结果集, 返回收集器
     * @return 收集器
     * @throws SQLRuntimeException     数据库异常
     * @throws EntityNotFoundException 查询结果为空
     */
    Record<T> querySql() throws SQLRuntimeException, EntityNotFoundException {
        // sql组装执行
        String       sql           = grammar.generateSql(SqlType.SELECT);
        List<String> parameterList = grammar.getParameterList(SqlType.SELECT);
        return queryOrFail(sql, parameterList);
    }

    /**
     * 执行sql, 处理jdbc结果集, 返回收集器
     * @return 收集器
     * @throws SQLRuntimeException     数据库异常
     * @throws EntityNotFoundException 查询结果为空
     */
    RecordList<T> querySqlList() throws SQLRuntimeException, EntityNotFoundException {
        // sql组装执行
        String       sql           = grammar.generateSql(SqlType.SELECT);
        List<String> parameterList = grammar.getParameterList(SqlType.SELECT);
        return queryList(sql, parameterList);
    }

    @Override
    public void dealChunk(int num, Chunk<T> chunk) throws SQLRuntimeException {
        int     offset = 0;
        boolean flag;
        do {
            Builder cloneBuilder = clone();
            cloneBuilder.limit(offset, num);
            String        sql           = cloneBuilder.grammar.generateSql(SqlType.SELECT);
            List<String>  parameterList = cloneBuilder.grammar.getParameterList(SqlType.SELECT);
            RecordList<T> records       = queryList(sql, parameterList);
            flag = chunk.deal(records) && (records.size() == num);
            offset += num;
        } while (flag);
    }


    /**
     * 执行sql, 返回收影响的行数
     * @return 影响的行数
     * @throws SQLRuntimeException 数据库异常
     */
    int updateSql(SqlType sqlType) throws SQLRuntimeException {
        if (sqlType != SqlType.INSERT && !grammar.hasWhere())
            throw new ConfirmOperationException("You made a risky operation without where conditions, use where(1) " +
                "for sure");
        // sql组装执行
        String       sql           = grammar.generateSql(sqlType);
        List<String> parameterList = grammar.getParameterList(sqlType);
        return execute(sql, parameterList);
    }

    /**
     * 获取指定的数据库连接
     * @param isWrite 写连接
     * @return 数据库连接
     * @throws SQLRuntimeException 数据库连接获取失败
     */
    private Connection theConnection(boolean isWrite) throws SQLRuntimeException {
        if (inTransaction()) {
            return getLocalThreadConnection();
        } else {
            dataSource.setWrite(isWrite);
            try {
                return dataSource.getConnection();
            } catch (SQLException e) {
                throw new SQLRuntimeException(e.getMessage(), e);
            }
        }
    }

    /**
     * 关闭数据库连接
     * @param connection 数据库连接
     * @throws SQLRuntimeException 关闭连接出错
     */
    private static void connectionClose(Connection connection) throws SQLRuntimeException {
        try {
            connection.close();
        } catch (SQLException e) {
            throw new SQLRuntimeException(e.getMessage(), e);
        }
    }

    /**
     * 执行sql
     * @param connection    数据库连接
     * @param sql           查询语句
     * @param parameterList 参数绑定
     * @return 预执行对象
     * @throws SQLException sql错误
     */
    private PreparedStatement executeSql(Connection connection, String sql, Collection<String> parameterList)
        throws SQLException {
        // 日志记录
        model.log(sql, parameterList);
        // 预执行
        PreparedStatement preparedStatement = connection.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY,
            ResultSet.CONCUR_READ_ONLY);
        // 参数绑定
        int i = 1;
        for (String parameter : parameterList) {
            preparedStatement.setString(i++, parameter);
        }
        // 返回预执行对象
        return preparedStatement;
    }

    /**
     * 执行闭包生成sql
     * @param closure  闭包
     * @param wholeSql 是否生成完整sql
     * @return sql
     */
    private String generateSql(GenerateSqlPart<T> closure, boolean wholeSql) {
        Builder<T>   subBuilder    = closure.generate(getNewSelf());
        List<String> parameterList = subBuilder.grammar.getParameterList(SqlType.SUBQUERY);
        for (String parameter : parameterList) {
            grammar.pushWhereParameter(parameter);
        }
        SqlType sqlType = wholeSql ? SqlType.SELECT : SqlType.SUBQUERY;
        return FormatUtil.bracket(subBuilder.grammar.generateSql(sqlType));
    }

    /**
     * 将数据库连接保存在线程内
     * @param connection 数据库连接
     */
    private void setLocalThreadConnection(Connection connection) {
        localThreadConnectionList.put(localThreadConnectionListName(), connection);
    }

    /**
     * 从线程内取出数据库连接
     * @return 数据库连接
     */
    private Connection getLocalThreadConnection() {
        return localThreadConnectionList.get(localThreadConnectionListName());
    }

    /**
     * 移除线程内的数据库连接
     */
    private void removeLocalThreadConnection() {
        localThreadConnectionList.remove(localThreadConnectionListName());
    }

    private String localThreadConnectionListName() {
        String processName        = ManagementFactory.getRuntimeMXBean().getName();
        String threadName         = Thread.currentThread().getName();
        String className          = getClass().toString();
        int    dataSourceHashCode = dataSource.hashCode();
        return processName + threadName + className + dataSourceHashCode;

    }

}
