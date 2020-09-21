package gaarason.database.connection;

import gaarason.database.contract.connection.GaarasonDataSource;

import javax.sql.DataSource;
import java.util.List;

public class GaarasonDataSourceBuilder {

    public static GaarasonDataSourceBuilder create() {
        return new GaarasonDataSourceBuilder();
    }

    public GaarasonDataSource build(List<DataSource> masterDataSourceList) {
        return new GaarasonDataSourceWrapper(masterDataSourceList);
    }

    public GaarasonDataSource build(List<DataSource> masterDataSourceList, List<DataSource> slaveDataSourceList) {
        return new GaarasonDataSourceWrapper(masterDataSourceList, slaveDataSourceList);
    }

}
