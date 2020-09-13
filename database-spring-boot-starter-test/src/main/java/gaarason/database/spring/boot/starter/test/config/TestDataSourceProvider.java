package gaarason.database.spring.boot.starter.test.config;

import gaarason.database.connection.GaarasonDataSourceProvider;

import javax.sql.DataSource;

public class TestDataSourceProvider extends GaarasonDataSourceProvider {

    public TestDataSourceProvider(){
        hasSlave = true;
    }

    public DataSource getRealDataSource() {
        return (!hasSlave || isWrite || isInTransaction()) ? masterDataSource.get() : slaveDataSource.get();
    }

}