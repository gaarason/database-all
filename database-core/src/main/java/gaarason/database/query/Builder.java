package gaarason.database.query;

import gaarason.database.connections.ProxyDataSource;
import gaarason.database.contracts.Grammar;
import gaarason.database.contracts.builder.*;
import gaarason.database.contracts.function.Chunk;
import gaarason.database.contracts.function.GenerateSqlPart;
import gaarason.database.core.lang.Nullable;
import gaarason.database.eloquent.Model;
import gaarason.database.eloquent.Paginate;
import gaarason.database.eloquent.Record;
import gaarason.database.eloquent.RecordList;
import gaarason.database.eloquent.enums.SqlType;
import gaarason.database.exception.*;
import gaarason.database.support.RecordFactory;
import gaarason.database.utils.ExceptionUtil;
import gaarason.database.utils.FormatUtil;

import java.lang.management.ManagementFactory;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

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
    private final ProxyDataSource proxyDataSource;

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

    public Builder(ProxyDataSource proxyDataSource, Model<T> model, Class<T> entityClass) {
        this.proxyDataSource = proxyDataSource;
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

    /**
     * 克隆当前查询构造器
     * @return 查询构造器
     * @throws CloneNotSupportedRuntimeException 克隆异常
     */
    @Override
    @SuppressWarnings("unchecked")
    public Builder<T> clone() throws CloneNotSupportedRuntimeException {
        try {
            // 浅拷贝
            Builder<T> builder = (Builder<T>) super.clone();
            // 深拷贝
            builder.grammar = grammar.deepCopy();
            return builder;
        } catch (CloneNotSupportedException e) {
            throw new CloneNotSupportedRuntimeException(e.getMessage(), e);
        }
    }
    /**
     * 带总数的分页
     * @param currentPage 当前页
     * @param perPage     每页数量
     * @return 分页对象
     * @throws SQLRuntimeException 数据库异常
     * @throws CloneNotSupportedRuntimeException 克隆异常
     */
    @Override
    public Paginate<T> paginate(int currentPage, int perPage)
        throws SQLRuntimeException, CloneNotSupportedRuntimeException {
        Long    count = clone().count("*");
        List<T> list  = limit((currentPage - 1) * perPage, perPage).get().toObjectList();
        return new Paginate<>(list, currentPage, perPage, count.intValue());
    }

    /**
     * 不带总数的分页
     * @param currentPage 当前页
     * @param perPage     每页数量
     * @return 分页对象
     * @throws SQLRuntimeException 数据库异常
     */
    @Override
    public Paginate<T> simplePaginate(int currentPage, int perPage) throws SQLRuntimeException {
        List<T> list = limit((currentPage - 1) * perPage, perPage).get().toObjectList();
        return new Paginate<>(list, currentPage, perPage);
    }

    /**
     * 数据库事物开启
     * @throws SQLRuntimeException 数据库异常
     * @throws NestedTransactionException 构建
     */
    @Override
    public void begin() throws SQLRuntimeException, NestedTransactionException {
        synchronized (proxyDataSource) {
            if (proxyDataSource.isInTransaction()) {
                throw new NestedTransactionException();
            }
            try {
                proxyDataSource.setInTransaction();
                Connection connection = proxyDataSource.getConnection();
                connection.setAutoCommit(false);
                setLocalThreadConnection(connection);
            } catch (SQLException e) {
                throw new SQLRuntimeException(e.getMessage(), e);
            }
        }
    }

    /**
     * 数据库事物提交
     * @throws SQLRuntimeException 数据库异常
     */
    @Override
    public void commit() throws SQLRuntimeException {
        try {
            Objects.requireNonNull(getLocalThreadConnection()).commit();
            getLocalThreadConnection().close();
        } catch (SQLException e) {
            throw new SQLRuntimeException(e.getMessage(), e);
        } finally {
            removeLocalThreadConnection();
            proxyDataSource.setOutTransaction();
        }
    }

    /**
     * 数据库事物回滚
     * @throws SQLRuntimeException 数据库异常
     */
    @Override
    public void rollBack() throws SQLRuntimeException {
        try {
            getLocalThreadConnection().rollback();
            getLocalThreadConnection().close();
        } catch (SQLException e) {
            throw new SQLRuntimeException(e.getMessage(), e);
        } finally {
            removeLocalThreadConnection();
            proxyDataSource.setOutTransaction();
        }
    }

    /**
     * 当前线程是否在事物中
     * @return 是否在事物中
     */
    @Override
    public boolean inTransaction() {
        return proxyDataSource.isInTransaction();
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
            Builder<T> cloneBuilder = clone();
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
        // 如果在事物中, 那么取已经开启事物的 Connection
        if (inTransaction()) {
            return getLocalThreadConnection();
        }
        // 否则根据读写规则, 取新的 Connection
        else {
            synchronized (proxyDataSource) {
                proxyDataSource.setWrite(isWrite);
                try {
                    return proxyDataSource.getConnection();
                } catch (SQLException e) {
                    throw new SQLRuntimeException(e.getMessage(), e);
                }
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
        Connection connection = localThreadConnectionList.get(localThreadConnectionListName());
        if (connection == null) {
            throw new InternalConcurrentException(
                "Get an null value in localThreadConnectionList with key[" + localThreadConnectionListName() + "].");
        }
        return connection;
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
        int    dataSourceHashCode = proxyDataSource.hashCode();
        return processName + threadName + className + dataSourceHashCode;
    }

}
