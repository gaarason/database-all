package gaarason.database.contract.connection;

import gaarason.database.contract.transaction.SavepointManager;
import gaarason.database.contract.transaction.TransactionManager;

import javax.sql.DataSource;
import java.util.List;

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
}
