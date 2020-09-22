package gaarason.database.contract.connection;

import gaarason.database.exception.NestedTransactionException;
import gaarason.database.exception.SQLRuntimeException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.List;

public interface GaarasonDataSource extends DataSource {

    /**
     * 当前线程 数据库事物开启
     * @throws SQLRuntimeException        数据库异常
     * @throws NestedTransactionException 数据库书屋重复开启异常
     */
    void begin() throws SQLRuntimeException, NestedTransactionException;

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
     * 获取本地 connection
     * @return
     */
    Connection getLocalConnection(boolean isWrite);

    void connectionClose(Connection connection) throws SQLRuntimeException;


//    /**
//     * 是否在事物中
//     * @return
//     */
//    boolean isInTransaction();
//
//    /**
//     * 设置进入事物标记
//     */
//    void setInTransaction();
//
//    /**
//     * 移除计入事物标记
//     */
//    void setOutTransaction();

//    /**
//     * 获取读写
//     * @return
//     */
//    boolean isWrite();
//
//    /**
//     * 设置读写
//     */
//    void setWrite(boolean bool);

    /**
     * 得到 DataSource
     * @return DataSource
     */
    DataSource getRealDataSource();


    /**
     * 获取主要连接(写)
     * @return
     */
    List<DataSource> getMasterDataSourceList();

    /**
     * 获取从连接(读)
     * @return
     */
    List<DataSource> getSlaveDataSourceList();
}
