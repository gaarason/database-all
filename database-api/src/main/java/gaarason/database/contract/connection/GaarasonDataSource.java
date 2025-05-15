package gaarason.database.contract.connection;

import gaarason.database.config.QueryBuilderConfig;
import gaarason.database.contract.transaction.SavepointManager;
import gaarason.database.contract.transaction.TransactionManager;
import gaarason.database.core.Container;
import gaarason.database.exception.SQLRuntimeException;

import javax.sql.DataSource;
import java.util.List;

/**
 * DataSource接口
 * @author xt
 */
public interface GaarasonDataSource extends DataSource, SavepointManager, TransactionManager, Container.Keeper {

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

    /**
     * 数据库类型
     * @return DatabaseType
     * @throws SQLRuntimeException 数据库异常
     */
    QueryBuilderConfig getQueryBuilder() throws SQLRuntimeException;

    /**
     * 加入一个事务结束后的事件回调
     * @param runnable 事件回调
     */
    void addEvent(Runnable runnable);

    /**
     * 加入一个事务结束后的事件回调
     * @param runnableList 事件回调列表
     */
    void addEvent(List<Runnable> runnableList);
}
