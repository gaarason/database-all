package gaarason.database.config;

import gaarason.database.contract.connection.GaarasonDataSource;

import javax.sql.DataSource;
import java.util.List;

/**
 * 数据源
 * @author xt
 * @since 2021/11/30 6:05 下午
 */
public interface GaarasonDataSourceConfig {

    /**
     * 单个主库的数据源
     * @param masterDataSource 原数据源
     * @return 数据源
     */
    GaarasonDataSource build(DataSource masterDataSource);

    /**
     * 多个主库的数据源
     * @param masterDataSourceList 原写数据源列表
     * @return 数据源
     */
    GaarasonDataSource build(List<DataSource> masterDataSourceList);

    /**
     * 多个主库与从库的数据源
     * @param masterDataSourceList 原写数据源列表
     * @param slaveDataSourceList  原读数据源列表
     * @return 数据源
     */
    GaarasonDataSource build(List<DataSource> masterDataSourceList, List<DataSource> slaveDataSourceList);

}
