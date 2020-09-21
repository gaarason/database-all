package gaarason.database.connection;

import gaarason.database.contract.connection.GaarasonDataSource;
import gaarason.database.exception.InternalConcurrentException;
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
    protected boolean hasSlave = false;

    /**
     * 单前线程中的 ProxyDataSource对象 是否处于数据库事物中
     */
    protected ThreadLocal<Boolean> inTransaction = ThreadLocal.withInitial(() -> false);

    /**
     * 是否写连接
     */
    @Setter
    @Getter
    protected boolean isWrite = false;

    protected ThreadLocal<DataSource> masterDataSource = ThreadLocal.withInitial(() -> {
        // TODO 权重选择
        return masterDataSourceList.get((new Random()).nextInt(masterDataSourceList.size()));
    });

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
    }

    public GaarasonDataSourceWrapper() {
    }

    public boolean isInTransaction() {
        return inTransaction.get();
    }

    public void setInTransaction() {
        inTransaction.set(true);
    }

    public void setOutTransaction() {
        inTransaction.remove();
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
            throw new InternalConcurrentException("Get an null value in ProxyDataSource object.");
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

}
