package gaarason.database.contract.connection;

import gaarason.database.contract.transaction.SavepointManager;
import gaarason.database.contract.transaction.TransactionManager;
import gaarason.database.eloquent.appointment.DatabaseType;
import gaarason.database.exception.SQLRuntimeException;

import javax.sql.DataSource;
import java.util.List;

/**
 * DataSource接口
 * @author xt
 */
public interface GaarasonDataSource extends DataSource, SavepointManager, TransactionManager {

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
    DatabaseType getDatabaseType() throws SQLRuntimeException;
}
