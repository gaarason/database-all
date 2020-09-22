package gaarason.database.connection;

import gaarason.database.contract.connection.GaarasonDataSource;
import gaarason.database.eloquent.appointment.Propagation;
import gaarason.database.exception.InternalConcurrentException;
import gaarason.database.exception.NestedTransactionException;
import gaarason.database.exception.SQLRuntimeException;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

@ToString
public class GaarasonDataSourceWrapper implements GaarasonDataSource {

    /**
     * 事物中的 Connection
     */
    protected final ThreadLocal<Connection> localThreadConnection = new ThreadLocal<>();

    /**
     * 写连接
     */
    @Getter
    protected List<DataSource> masterDataSourceList;

    /**
     * 读连接
     */
    @Getter
    protected List<DataSource> slaveDataSourceList;

    /**
     * 是否主从(读写分离)
     */
    protected final boolean hasSlave;

    /**
     * 当前线程中的 GaarasonDataSource 对象 是否处于数据库事物中
     */
    protected ThreadLocal<Boolean> inTransaction = ThreadLocal.withInitial(() -> false);

    /**
     * 是否写连接
     */
    protected volatile boolean isWrite = false;

    /**
     * 当前线程的写连接
     */
    protected ThreadLocal<DataSource> masterDataSource = ThreadLocal.withInitial(() -> {
        // TODO 权重选择
        return masterDataSourceList.get((new Random()).nextInt(masterDataSourceList.size()));
    });

    /**
     * 当前线程的读连接
     */
    protected ThreadLocal<DataSource> slaveDataSource = ThreadLocal.withInitial(() -> {
        // TODO 权重选择
        return slaveDataSourceList.get((new Random()).nextInt(slaveDataSourceList.size()));
    });

    public GaarasonDataSourceWrapper(List<DataSource> masterDataSourceList, List<DataSource> slaveDataSourceList) {
        this.masterDataSourceList = masterDataSourceList;
        this.slaveDataSourceList = slaveDataSourceList;
        hasSlave = true;
    }

    public GaarasonDataSourceWrapper(List<DataSource> masterDataSourceList) {
        this.masterDataSourceList = masterDataSourceList;
        hasSlave = false;
    }

    @Override
    public void begin() {
        synchronized (this) {
            if (isInTransaction()) {
                throw new NestedTransactionException();
            }
            try {
                Connection connection = getConnection();
                connection.setAutoCommit(false);
                localThreadConnection.set(connection);
            } catch (SQLException e) {
                throw new SQLRuntimeException(e.getMessage(), e);
            }
        }
    }

    @Override
    public void commit() {
        try {
            Connection connection = localThreadConnection.get();
            connection.commit();
            connection.close();
        } catch (SQLException e) {
            throw new SQLRuntimeException(e.getMessage(), e);
        } finally {
            localThreadConnection.remove();
        }
    }

    @Override
    public void rollBack() {
        try {
            Connection connection = localThreadConnection.get();
            connection.rollback();
            connection.close();
        } catch (SQLException e) {
            throw new SQLRuntimeException(e.getMessage(), e);
        } finally {
            localThreadConnection.remove();
        }
    }

    @Override
    public Connection getLocalConnection(boolean isWrite) {
        if (isInTransaction()) {
            return localThreadConnection.get();
        } else {
            synchronized (this) {
                this.isWrite = isWrite;
                try {
                    return getConnection();
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
    public void connectionClose(Connection connection) throws SQLRuntimeException {
        try {
            if (!isInTransaction())
                connection.close();
        } catch (SQLException e) {
            throw new SQLRuntimeException(e.getMessage(), e);
        }
    }

    /**
     * 得到 DataSource
     * @return DataSource
     */
    @Override
    public DataSource getRealDataSource() {
        return (!hasSlave || isWrite || isInTransaction()) ? masterDataSource.get() : slaveDataSource.get();
    }

    @Override
    public Connection getConnection() throws SQLException {
        DataSource realDataSource = getRealDataSource();
        Connection connection     = realDataSource.getConnection();
        if (null == connection) {
            throw new InternalConcurrentException("Get an null value in GaarasonDataSourceWrapper object.");
        }
        return connection;
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
     * 当前线程是否在事物中
     * @return 是否事物中
     */
    protected boolean isInTransaction() {
        return localThreadConnection.get() != null;
    }

    public static class TransactionInfo{
        /**
         *
         */
        protected final Connection connection;
        protected final Propagation propagation;
        protected int counter = 0;

        public TransactionInfo(Connection connection,  Propagation propagation){
            this.connection = connection;
            this.propagation = propagation;
        }
    }
}
