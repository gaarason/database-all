package gaarason.database.connections;

import gaarason.database.contracts.GaarasonDataSource;

import javax.sql.DataSource;
import java.util.List;

public class GaarasonDataSourceBuilder {

    public static GaarasonDataSourceBuilder create() {
        return new GaarasonDataSourceBuilder();
    }

    public GaarasonDataSource build(List<DataSource> masterDataSourceList) {
        return new ProxyDataSource(masterDataSourceList);
    }

    public GaarasonDataSource build(List<DataSource> masterDataSourceList, List<DataSource> slaveDataSourceList) {
        return new ProxyDataSource(masterDataSourceList, slaveDataSourceList);
    }

}
