package gaarason.database.query;

import gaarason.database.contracts.GaarasonDataSource;
import gaarason.database.contracts.Grammar;
import gaarason.database.contracts.builder.*;
import gaarason.database.contracts.function.Chunk;
import gaarason.database.contracts.function.ExecSqlWithinConnection;
import gaarason.database.contracts.function.GenerateSqlPart;
import gaarason.database.contracts.function.RelationshipRecordWith;
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
import gaarason.database.utils.ObjectUtil;

import java.lang.management.ManagementFactory;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

abstract public class Builder<T, K> implements Cloneable, Where<T, K>, Having<T, K>, Union<T, K>, Support<T, K>,
    From<T, K>, Execute<T, K>, With<T, K>, Select<T, K>, OrderBy<T, K>, Limit<T, K>, Group<T, K>, Value<T, K>,
    Data<T, K>, Transaction<T, K>, Aggregates<T, K>, Paginator<T, K>, Lock<T, K>, Native<T, K>, Join<T, K>,
    Ability<T, K> {

    /**
     * 数据实体类
     */
    Class<T> entityClass;

    /**
     * 数据库连接
     */
    private final GaarasonDataSource gaarasonDataSource;

    /**
     * connection缓存
     */
    private static Map<String, Connection> localThreadConnectionMap = new ConcurrentHashMap<>();

    /**
     * sql生成器
     */
    Grammar grammar;

    /**
     * 数据模型
     */
    protected Model<T, K> model;

    public Builder(GaarasonDataSource gaarasonDataSource, Model<T, K> model, Class<T> entityClass) {
        this.gaarasonDataSource = gaarasonDataSource;
        this.model = model;
        this.entityClass = entityClass;
        grammar = grammarFactory();
    }

    /**
     * 得到一个全新的查询构造器
     * @return 查询构造器
     */
    private Builder<T, K> getNewSelf() {
        return model.newQuery();
    }

    /**
     * 执行闭包生成sqlPart
     * @param closure 闭包
     * @return sqlPart eg:(`id`="3" and `age` between "12" and "19")
     */
    String generateSqlPart(GenerateSqlPart closure) {
        return generateSql(closure, false);
    }

    /**
     * 执行闭包生成完整sql
     * @param closure 闭包
     * @return sqlPart eg:(select * from `student` where `id`="3" and `age` between "12" and "19")
     */
    String generateSql(GenerateSqlPart closure) {
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
    public Builder<T, K> clone() throws CloneNotSupportedRuntimeException {
        try {
            // 浅拷贝
            Builder<T, K> builder = (Builder<T, K>) super.clone();
            // 深拷贝
            builder.grammar = grammar.deepCopy();
            return builder;
        } catch (CloneNotSupportedException e) {
            throw new CloneNotSupportedRuntimeException(e.getMessage(), e);
        }
    }

    /**
     * 渴求式关联
     * @param column 所关联的Model(当前模块的属性名)
     * @return 关联的Model的查询构造器
     */
    @Override
    public Builder<T, K> with(String column) {
        return with(column, builder -> builder, record -> record);
    }

    /**
     * 渴求式关联
     * @param column         所关联的Model(当前模块的属性名)
     * @param builderClosure 所关联的Model的查询构造器约束
     * @return 关联的Model的查询构造器
     */
    @Override
    public Builder<T, K> with(String column, GenerateSqlPart builderClosure) {
        return with(column, builderClosure, record -> record);
    }

    /**
     * 渴求式关联
     * @param column         所关联的Model(当前模块的属性名)
     * @param builderClosure 所关联的Model的查询构造器约束
     * @param recordClosure  所关联的Model的再一级关联
     * @return 关联的Model的查询构造器
     */
    @Override
    public Builder<T, K> with(String column, GenerateSqlPart builderClosure,
                              RelationshipRecordWith recordClosure) {
        grammar.pushWith(column, builderClosure, recordClosure);
        return this;
    }

    /**
     * 带总数的分页
     * @param currentPage 当前页
     * @param perPage     每页数量
     * @return 分页对象
     * @throws SQLRuntimeException               数据库异常
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
     * @throws SQLRuntimeException        数据库异常
     * @throws NestedTransactionException 构建
     */
    @Override
    public void begin() throws SQLRuntimeException, NestedTransactionException {
        synchronized (gaarasonDataSource) {
            if (gaarasonDataSource.isInTransaction()) {
                throw new NestedTransactionException();
            }
            try {
                gaarasonDataSource.setInTransaction();
                Connection connection = gaarasonDataSource.getConnection();
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
            gaarasonDataSource.setOutTransaction();
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
            gaarasonDataSource.setOutTransaction();
        }
    }

    /**
     * 当前线程是否在事物中
     * @return 是否在事物中
     */
    @Override
    public boolean inTransaction() {
        return gaarasonDataSource.isInTransaction();
    }

    @Override
    public boolean transaction(Runnable runnable, int maxAttempts, boolean throwException) {
        for (int currentAttempt = 1; currentAttempt <= maxAttempts; currentAttempt++) {
            begin();
            try {
                runnable.run();
                commit();
                return true;
            } catch (Throwable e) {
                rollBack();
                if (!ExceptionUtil.causedByDeadlock(e)) {
                    if (throwException)
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

    @Override
    public int forceDelete() throws SQLRuntimeException {
        return updateSql(SqlType.DELETE);
    }

    @Nullable
    @Override
    public Record<T, K> query(String sql, Collection<String> parameters) throws SQLRuntimeException {
        try {
            return queryOrFail(sql, parameters);
        } catch (EntityNotFoundException e) {
            return null;
        }
    }

    @Override
    public Record<T, K> queryOrFail(String sql, Collection<String> parameters)
        throws SQLRuntimeException, EntityNotFoundException {
        return doSomethingInConnection((preparedStatement) -> {
            ResultSet resultSet = preparedStatement.executeQuery();
            return RecordFactory.newRecord(entityClass, model, resultSet, sql);
        }, sql, parameters, false);
    }

    @Override
    public RecordList<T, K> queryList(String sql, Collection<String> parameters) throws SQLRuntimeException {
        return doSomethingInConnection((preparedStatement) -> {
            ResultSet resultSet = preparedStatement.executeQuery();
            return RecordFactory.newRecordList(entityClass, model, resultSet, sql);
        }, sql, parameters, false);
    }

    @Override
    public int execute(String sql, Collection<String> parameters) throws SQLRuntimeException {
        return doSomethingInConnection(PreparedStatement::executeUpdate, sql, parameters, true);
    }

    @Override
    public List<K> executeGetIds(String sql, Collection<String> parameters) throws SQLRuntimeException {
        return doSomethingInConnection((preparedStatement) -> {
            List<K> ids = new ArrayList<>();
            // 执行
            int affectedRows = preparedStatement.executeUpdate();
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
    public K executeGetId(String sql, Collection<String> parameters) throws SQLRuntimeException {
        return doSomethingInConnection((preparedStatement) -> {
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
     * @param closure    闭包
     * @param sql        带占位符的sql
     * @param parameters sql的参数
     * @param isWrite    是否写(主)链接
     * @param <U>        响应类型
     * @return 响应
     * @throws SQLRuntimeException 数据库异常
     */
    protected <U> U doSomethingInConnection(ExecSqlWithinConnection<U> closure, String sql,
                                            Collection<String> parameters, boolean isWrite) throws SQLRuntimeException {
        // 获取连接
        Connection connection = theConnection(isWrite);
        try {
            // 参数准备
            PreparedStatement preparedStatement = executeSql(connection, sql, parameters);
            // 执行
            return closure.exec(preparedStatement);
        } catch (EntityNotFoundException e) {
            throw new EntityNotFoundException(String.format(sql.replace(" ? ", "\"%s\""), parameters.toArray()));
        } catch (Throwable e) {
            throw new SQLRuntimeException(sql, parameters, e.getMessage(), e);
        } finally {
            // 关闭连接
            if (!inTransaction()) {
                connectionClose(connection);
            }
        }
    }

    /**
     * 获取主键
     * @param generatedKeys 数据集
     * @return 主键
     * @throws SQLException                      数据库异常
     * @throws PrimaryKeyTypeNotSupportException 不支持的主键类型
     */
    private K getGeneratedKeys(ResultSet generatedKeys) throws SQLException, PrimaryKeyTypeNotSupportException {
        Class<K> primaryKeyClass = model.getPrimaryKeyClass();
        if (Byte.class.equals(primaryKeyClass) || byte.class.equals(primaryKeyClass)) {
            return ObjectUtil.typeCast(generatedKeys.getByte(1));
        } else if (Integer.class.equals(primaryKeyClass) || int.class.equals(primaryKeyClass)) {
            return ObjectUtil.typeCast(generatedKeys.getInt(1));
        } else if (Long.class.equals(primaryKeyClass) || long.class.equals(primaryKeyClass)) {
            return ObjectUtil.typeCast(generatedKeys.getLong(1));
        } else if (String.class.equals(primaryKeyClass)) {
            return ObjectUtil.typeCast(generatedKeys.getString(1));
        } else if (Object.class.equals(primaryKeyClass)) {
            return ObjectUtil.typeCast(generatedKeys.getString(1));
        }
        throw new PrimaryKeyTypeNotSupportException("Primary key type [" + primaryKeyClass + "] not support get " +
            "generated keys yet.");
    }

    /**
     * 执行sql, 处理jdbc结果集, 返回收集器
     * @return 收集器
     * @throws SQLRuntimeException     数据库异常
     * @throws EntityNotFoundException 查询结果为空
     */
    Record<T, K> querySql() throws SQLRuntimeException, EntityNotFoundException {
        // sql组装执行
        String                sql           = grammar.generateSql(SqlType.SELECT);
        List<String>          parameterList = grammar.getParameterList(SqlType.SELECT);
        Map<String, Object[]> columnMap     = grammar.pullWith();
        Record<T, K>          record        = queryOrFail(sql, parameterList);
        for (String column : columnMap.keySet()) {
            Object[] objects = columnMap.get(column);
            record.with(column, (GenerateSqlPart) objects[0], (RelationshipRecordWith) objects[1]);
        }
        return record;
    }

    /**
     * 执行sql, 处理jdbc结果集, 返回收集器
     * @return 收集器
     * @throws SQLRuntimeException     数据库异常
     * @throws EntityNotFoundException 查询结果为空
     */
    RecordList<T, K> querySqlList() throws SQLRuntimeException, EntityNotFoundException {
        // sql组装执行
        String                sql           = grammar.generateSql(SqlType.SELECT);
        List<String>          parameterList = grammar.getParameterList(SqlType.SELECT);
        Map<String, Object[]> columnMap     = grammar.pullWith();
        RecordList<T, K>      records       = queryList(sql, parameterList);
        for (String column : columnMap.keySet()) {
            Object[] objects = columnMap.get(column);
            records.with(column, (GenerateSqlPart) objects[0], (RelationshipRecordWith) objects[1]);
        }
        return records;
    }

    @Override
    public void dealChunk(int num, Chunk<T, K> chunk) throws SQLRuntimeException {
        int     offset = 0;
        boolean flag;
        do {
            Builder<T, K> cloneBuilder = clone();
            cloneBuilder.limit(offset, num);
            String                sql           = cloneBuilder.grammar.generateSql(SqlType.SELECT);
            List<String>          parameterList = cloneBuilder.grammar.getParameterList(SqlType.SELECT);
            Map<String, Object[]> columnMap     = grammar.pullWith();
            RecordList<T, K>      records       = queryList(sql, parameterList);
            for (String column : columnMap.keySet()) {
                Object[] objects = columnMap.get(column);
                records.with(column, (GenerateSqlPart) objects[0], (RelationshipRecordWith) objects[1]);
            }
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
            synchronized (gaarasonDataSource) {
                gaarasonDataSource.setWrite(isWrite);
                try {
                    return gaarasonDataSource.getConnection();
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
        // 预执行 ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY
        PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
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
    private String generateSql(GenerateSqlPart closure, boolean wholeSql) {
        Builder<?, ?> subBuilder    = closure.generate(getNewSelf());
        List<String>  parameterList = subBuilder.grammar.getParameterList(SqlType.SUBQUERY);
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
        localThreadConnectionMap.put(localThreadConnectionListName(), connection);
    }

    /**
     * 从线程内取出数据库连接
     * @return 数据库连接
     */
    private Connection getLocalThreadConnection() {
        Connection connection = localThreadConnectionMap.get(localThreadConnectionListName());
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
        localThreadConnectionMap.remove(localThreadConnectionListName());
    }

    private String localThreadConnectionListName() {
        String processName        = ManagementFactory.getRuntimeMXBean().getName();
        String threadName         = Thread.currentThread().getName();
        String className          = getClass().toString();
        int    dataSourceHashCode = gaarasonDataSource.hashCode();
        return processName + threadName + className + dataSourceHashCode;
    }

}
