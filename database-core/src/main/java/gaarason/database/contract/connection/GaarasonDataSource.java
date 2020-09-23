package gaarason.database.contract.connection;

import gaarason.database.contract.transaction.SavepointManager;
import gaarason.database.exception.SQLRuntimeException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.List;

public interface GaarasonDataSource extends DataSource, SavepointManager {

    /**
     * 当前线程 数据库事物开启
     * @throws SQLRuntimeException 数据库异常
     */
    void begin() throws SQLRuntimeException;

    /**
     * 当前线程 提交事物
     * @throws SQLRuntimeException 数据库异常
     */
    void commit() throws SQLRuntimeException;

    /**
     * 当前线程 回滚事物
     * @throws SQLRuntimeException 数据库异常
     */
    void rollBack() throws SQLRuntimeException;

    /**
     * 当前线程 是否在事物中
     * @throws SQLRuntimeException 数据库异常
     */
    boolean isLocalThreadInTransaction() throws SQLRuntimeException;

    /**
     * 当前线程 获取本地 connection
     * 判断当前线程是否在事务中
     * @return Connection
     */
    Connection getLocalConnection(boolean isWrite) throws SQLRuntimeException;

    /**
     * 当前线程 关闭某个连接
     * @param connection 连接
     * @throws SQLRuntimeException 数据库异常
     */
    void localConnectionClose(Connection connection) throws SQLRuntimeException;


    /**
     * 获取主要连接(写)
     * @return data source 集合
     */
    List<DataSource> getMasterDataSourceList();

    /**
     * 获取从连接(读)
     * @return data source 集合
     */
    List<DataSource> getSlaveDataSourceList();
}
