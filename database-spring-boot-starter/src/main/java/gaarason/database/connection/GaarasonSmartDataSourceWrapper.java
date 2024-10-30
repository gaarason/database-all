package gaarason.database.connection;

import gaarason.database.core.Container;
import org.springframework.jdbc.datasource.SmartDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.List;

public class GaarasonSmartDataSourceWrapper extends GaarasonDataSourceWrapper implements SmartDataSource {

    public GaarasonSmartDataSourceWrapper(List<DataSource> masterDataSourceList,
            List<DataSource> slaveDataSourceList, Container container) {
        super(masterDataSourceList, slaveDataSourceList, container);
    }

    public GaarasonSmartDataSourceWrapper(List<DataSource> masterDataSourceList, Container container) {
        super(masterDataSourceList, container);
    }

    @Override
    public boolean shouldClose(Connection con) {
        return !isLocalThreadInTransaction();
    }
}
