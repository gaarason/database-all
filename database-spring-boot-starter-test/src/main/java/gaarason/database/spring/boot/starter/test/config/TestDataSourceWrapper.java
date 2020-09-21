package gaarason.database.spring.boot.starter.test.config;

import gaarason.database.connection.GaarasonDataSourceWrapper;

import javax.sql.DataSource;

public class TestDataSourceWrapper extends GaarasonDataSourceWrapper {

    public TestDataSourceWrapper() {
        hasSlave = true;
    }

    public DataSource getRealDataSource() {
        return (!hasSlave || isWrite || isInTransaction()) ? masterDataSource.get() : slaveDataSource.get();
    }

}