package gaarason.database.connection;

import gaarason.database.config.QueryBuilderConfig;
import gaarason.database.contract.connection.GaarasonDataSource;
import gaarason.database.core.Container;
import gaarason.database.exception.*;
import gaarason.database.lang.Nullable;
import gaarason.database.util.ObjectUtils;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Savepoint;
import java.util.*;
import java.util.logging.Logger;

/**
 * 以下事物相关的代码, 全部基于一个前提: 事务不可以跨线程执行
 * 事物传播性: 使用 nested .如果不存在事务，创建事务。如果存在事务，则嵌套在事务内，嵌套事务依赖外层事务提交，不进行独立事务提交。
 * 嵌套事务如果发生异常，则抛出异常，回滚嵌套事务的操作，回到开始嵌套事务的“保存点”，由外层事务的逻辑继续执行（外层捕获异常并处理即可）。
 * 嵌套事务如果不发生异常，则继续执行，不提交。由外层事务的逻辑继续执行，若外层事务后续发生异常，则回滚包括嵌套事务在内的所有事务。
 * @author xt
 */
public class GaarasonDataSourceWrapper extends Container.SimpleKeeper implements GaarasonDataSource {

    /**
     * 事物中的 Connection
     */
    protected final ThreadLocal<Connection> localThreadTransactionConnection = new ThreadLocal<>();

    /**
     * 事物中的 savepoint 列表
     * 事物嵌套是才会使用
     */
    protected final ThreadLocal<LinkedList<Savepoint>> localThreadTransactionSavepointLinkedList = ThreadLocal.withInitial(
        LinkedList::new);

    /**
     * 写连接
     */
    protected final List<DataSource> masterDataSourceList;

    /**
     * 读连接
     */
    protected final List<DataSource> slaveDataSourceList;

    /**
     * 是否主从(读写分离)
     */
    protected final boolean hasSlave;

    /**
     * 数据库类型
     */
    @Nullable
    protected QueryBuilderConfig queryBuilder;

    /**
     * 构造
     * @param masterDataSourceList (主)写数据源集合
     * @param slaveDataSourceList (从)读数据源集合
     * @param container 容器
     */
    GaarasonDataSourceWrapper(List<DataSource> masterDataSourceList, List<DataSource> slaveDataSourceList,
        Container container) {
        super(container);
        if (masterDataSourceList.isEmpty() || slaveDataSourceList.isEmpty()) {
            throw new AbnormalParameterException("The two list of data source should not be empty.");
        }
        this.masterDataSourceList = masterDataSourceList;
        this.slaveDataSourceList = slaveDataSourceList;
        hasSlave = true;
    }

    /**
     * 构造
     * @param masterDataSourceList (主)写数据源集合
     * @param container 容器
     */
    GaarasonDataSourceWrapper(List<DataSource> masterDataSourceList, Container container) {
        super(container);
        if (masterDataSourceList.isEmpty()) {
            throw new AbnormalParameterException("The list of data source should not be empty.");
        }
        this.masterDataSourceList = masterDataSourceList;
        this.slaveDataSourceList = new ArrayList<>();
        hasSlave = false;
    }

    @Override
    public void begin() {
        // 无已存在事物, 直接开启
        if (!isLocalThreadInTransaction()) {
            try {
                DataSource dataSource = getRealDataSource(true);
                Connection connection = dataSource.getConnection();
                connection.setAutoCommit(false);
                localThreadTransactionConnection.set(connection);
            } catch (SQLException e) {
                throw new SQLRuntimeException(e.getMessage(), e);
            }
        }
        // 保存点
        else {
            createSavepoint();
        }
    }

    @Override
    public void commit() {
        Connection connection = localThreadTransactionConnection.get();
        // 无已存在 savepoint, 直接提交
        if (localThreadTransactionSavepointLinkedList.get().isEmpty()) {
            try {
                connection.commit();
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                throw new SQLRuntimeException(e.getMessage(), e);
            } finally {
                connectionClose(connection);
                localThreadTransactionConnection.remove();
            }
        }
        // 移除 savepoint
        else {
            releaseSavepoint();
        }
    }

    @Override
    public void rollBack() {
        Connection connection = localThreadTransactionConnection.get();
        // 无已存在 savepoint, 直接回滚
        if (localThreadTransactionSavepointLinkedList.get().isEmpty()) {
            try {
                connection.rollback();
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                throw new SQLRuntimeException(e.getMessage(), e);
            } finally {
                connectionClose(connection);
                localThreadTransactionConnection.remove();
            }
        }
        // 回滚到 savepoint
        else {
            rollbackToSavepoint();
        }
    }

    /**
     * 关闭数据库连接
     * @param connection 数据库连接
     * @throws SQLRuntimeException 关闭连接出错
     */
    @Override
    public void localConnectionClose(Connection connection) throws SQLRuntimeException {
        try {
            if (!isLocalThreadInTransaction()) {
                connection.close();
            }
        } catch (SQLException e) {
            throw new SQLRuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public Connection getLocalConnection(boolean isWriteOrTransaction) throws SQLRuntimeException {
        // 事物中, 则返回之前的最新 Connection
        if (isLocalThreadInTransaction()) {
            return localThreadTransactionConnection.get();
        }
        // 不存在事务则返回当前线程的数据源的连接池中的 Connection
        try {
            DataSource realDataSource = getRealDataSource(isWriteOrTransaction);
            return realDataSource.getConnection();
        } catch (SQLException e) {
            throw new SQLRuntimeException(e.getMessage(), e);
        } catch (Throwable e) {
            throw new InternalConcurrentException("Get an null value in GaarasonDataSourceWrapper object.", e);
        }
    }

    @Override
    public Connection getConnection() {
        return getLocalConnection(true);
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return getRealDataSource().getConnection(username, password);
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return getRealDataSource().unwrap(iface);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return getRealDataSource().isWrapperFor(iface);
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return getRealDataSource().getLogWriter();
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        getRealDataSource().setLogWriter(out);
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return getRealDataSource().getLoginTimeout();
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        getRealDataSource().setLoginTimeout(seconds);
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return getRealDataSource().getParentLogger();
    }

    /**
     * 得到 DataSource, 不考虑事物
     * @return DataSource
     */
    protected DataSource getRealDataSource() {
        return getRealDataSource(true);
    }

    /**
     * 得到 DataSource, 考虑事物
     * @param isWriteOrTransaction 是否在事务中, 是否需要写连接
     * @return DataSource
     */
    protected DataSource getRealDataSource(boolean isWriteOrTransaction) {
        if (!hasSlave || isWriteOrTransaction) {
            return masterDataSourceList.get((new Random()).nextInt(masterDataSourceList.size()));
        } else {
            return slaveDataSourceList.get((new Random()).nextInt(slaveDataSourceList.size()));
        }
    }

    /**
     * 当前线程是否在事物中
     * @return 是否事物中
     */
    @Override
    public boolean isLocalThreadInTransaction() {
        return localThreadTransactionConnection.get() != null;
    }

    /**
     * 数据库连接关闭
     * @param connection 数据库连接
     * @throws ConnectionCloseException 关闭异常
     */
    protected static void connectionClose(Connection connection) throws ConnectionCloseException {
        try {
            connection.close();
        } catch (Throwable e) {
            throw new ConnectionCloseException(e.getMessage(), e);
        }
    }

    @Override
    public Savepoint createSavepoint() {
        Connection connection = localThreadTransactionConnection.get();
        try {
            Savepoint savepoint = connection.setSavepoint();
            localThreadTransactionSavepointLinkedList.get().add(savepoint);
            return savepoint;
        } catch (SQLException e) {
            throw new SQLRuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public void rollbackToSavepoint() {
        try {
            Connection connection = localThreadTransactionConnection.get();
            Savepoint savepointReal = localThreadTransactionSavepointLinkedList.get().removeLast();
            connection.rollback(savepointReal);
        } catch (SQLException e) {
            throw new SQLRuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public void releaseSavepoint() {
        Connection connection = localThreadTransactionConnection.get();
        Savepoint savepoint = localThreadTransactionSavepointLinkedList.get().removeLast();
        try {
            connection.releaseSavepoint(savepoint);
        } catch (SQLException e) {
            throw new SQLRuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public QueryBuilderConfig getQueryBuilder() throws SQLRuntimeException {
        QueryBuilderConfig localQueryBuilder = queryBuilder;
        if (localQueryBuilder == null) {
            synchronized (this) {
                localQueryBuilder = queryBuilder;
                if (localQueryBuilder == null) {
                    queryBuilder = localQueryBuilder = getQueryBuilder(this);
                }
            }
        }
        return localQueryBuilder;
    }

    /**
     * 通过数据源, 获取当前的数据库类型
     * @param dataSource 数据源
     * @return 数据库类型
     */
    protected QueryBuilderConfig getQueryBuilder(GaarasonDataSource dataSource) {
        List<QueryBuilderConfig> list = container.getBeans(QueryBuilderConfig.class);
        String databaseProductName;
        Connection connection = dataSource.getLocalConnection(true);
        try {
            databaseProductName = connection.getMetaData().getDatabaseProductName().toLowerCase(Locale.ENGLISH);
            for (QueryBuilderConfig queryBuilderConfig : list) {
                if (!ObjectUtils.isEmpty(queryBuilderConfig) && queryBuilderConfig.support(databaseProductName)) {
                    return queryBuilderConfig;
                }
            }
        } catch (Throwable e) {
            throw new SQLRuntimeException(e.getMessage(), e);
        } finally {
            dataSource.localConnectionClose(connection);
        }
        throw new TypeNotSupportedException("Database product name [" + databaseProductName + "] not supported yet.");
    }

    @Override
    public List<DataSource> getMasterDataSourceList() {
        return masterDataSourceList;
    }

    @Override
    public List<DataSource> getSlaveDataSourceList() {
        return slaveDataSourceList;
    }

    @Override
    public Container getContainer() {
        return container;
    }
}
