package gaarason.database.spring.boot.starter.test.config;

import gaarason.database.connections.ProxyDataSource;

import javax.sql.DataSource;

public class TestDataSource extends ProxyDataSource {

    public TestDataSource(){
        hasSlave = true;
    }

    public DataSource getRealDataSource() {
        return (!hasSlave || isWrite || isInTransaction()) ? masterDataSource.get() : slaveDataSource.get();
    }

}