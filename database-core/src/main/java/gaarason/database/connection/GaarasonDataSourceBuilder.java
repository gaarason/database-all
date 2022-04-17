package gaarason.database.connection;

import gaarason.database.contract.connection.GaarasonDataSource;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * GaarasonDataSource 构建器
 * 将 DataSource 转化为 GaarasonDataSource
 * @author xt
 */
public class GaarasonDataSourceBuilder {

    /**
     * 构造 GaarasonDataSource
     * @param masterDataSource 单个主要数据源
     * @return GaarasonDataSource
     */
    public static GaarasonDataSource build(DataSource masterDataSource) {
        return new GaarasonDataSourceWrapper(Collections.singletonList(masterDataSource));
    }

    /**
     * 构造 GaarasonDataSource
     * @param masterDataSourceList 多个主要数据源
     * @return GaarasonDataSource
     */
    public static GaarasonDataSource build(List<DataSource> masterDataSourceList) {
        return new GaarasonDataSourceWrapper(masterDataSourceList);
    }

    /**
     * 构造 GaarasonDataSource
     * @param masterDataSourceList 多个主要数据源
     * @param slaveDataSourceList 多个次要数据源
     * @return GaarasonDataSource
     */
    public static GaarasonDataSource build(List<DataSource> masterDataSourceList, List<DataSource> slaveDataSourceList) {
        return new GaarasonDataSourceWrapper(masterDataSourceList, slaveDataSourceList);
    }


}
