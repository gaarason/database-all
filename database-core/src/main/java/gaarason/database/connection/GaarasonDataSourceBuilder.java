package gaarason.database.connection;

import gaarason.database.bootstrap.ContainerBootstrap;
import gaarason.database.contract.connection.GaarasonDataSource;
import gaarason.database.core.Container;

import javax.sql.DataSource;
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
        return build(masterDataSource, ContainerBootstrap.build().autoBootstrap());
    }

    /**
     * 构造 GaarasonDataSource
     * @param masterDataSourceList 多个主要数据源
     * @return GaarasonDataSource
     */
    public static GaarasonDataSource build(List<DataSource> masterDataSourceList) {
        return build(masterDataSourceList, ContainerBootstrap.build().autoBootstrap());
    }

    /**
     * 构造 GaarasonDataSource
     * @param masterDataSourceList 多个主要数据源
     * @param slaveDataSourceList 多个次要数据源
     * @return GaarasonDataSource
     */
    public static GaarasonDataSource build(List<DataSource> masterDataSourceList,
        List<DataSource> slaveDataSourceList) {
        return build(masterDataSourceList, slaveDataSourceList, ContainerBootstrap.build().autoBootstrap());
    }

    /**
     * 构造 GaarasonDataSource
     * @param masterDataSource 单个主要数据源
     * @param container 容器
     * @return GaarasonDataSource
     */
    public static GaarasonDataSource build(DataSource masterDataSource, Container container) {
        return new GaarasonDataSourceWrapper(Collections.singletonList(masterDataSource), container);
    }

    /**
     * 构造 GaarasonDataSource
     * @param masterDataSourceList 多个主要数据源
     * @param container 容器
     * @return GaarasonDataSource
     */
    public static GaarasonDataSource build(List<DataSource> masterDataSourceList, Container container) {
        return new GaarasonDataSourceWrapper(masterDataSourceList, container);
    }

    /**
     * 构造 GaarasonDataSource
     * @param masterDataSourceList 多个主要数据源
     * @param slaveDataSourceList 多个次要数据源
     * @param container 容器
     * @return GaarasonDataSource
     */
    public static GaarasonDataSource build(List<DataSource> masterDataSourceList, List<DataSource> slaveDataSourceList,
        Container container) {
        return new GaarasonDataSourceWrapper(masterDataSourceList, slaveDataSourceList, container);
    }

}
