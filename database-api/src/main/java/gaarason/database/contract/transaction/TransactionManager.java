package gaarason.database.contract.transaction;

import gaarason.database.exception.SQLRuntimeException;

import java.sql.Connection;

/**
 * 事物管理器
 * @author xt
 */
public interface TransactionManager {

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
     * @return 是否在事物中
     * @throws SQLRuntimeException 数据库异常
     */
    boolean isLocalThreadInTransaction() throws SQLRuntimeException;

    /**
     * 当前线程 获取本地 connection
     * 判断当前线程是否在事务中
     * @param isWrite 是否写
     * @return Connection
     * @throws SQLRuntimeException 数据库异常
     */
    Connection getLocalConnection(boolean isWrite) throws SQLRuntimeException;

    /**
     * 当前线程 关闭某个连接
     * @param connection 连接
     * @throws SQLRuntimeException 数据库异常
     */
    void localConnectionClose(Connection connection) throws SQLRuntimeException;
}
