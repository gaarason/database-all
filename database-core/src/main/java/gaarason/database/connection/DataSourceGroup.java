package gaarason.database.connection;

import gaarason.database.util.ObjectUtils;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 数据源组: 封装一组 master(写) + slave(读) 数据源列表
 * @author xt
 */
public class DataSourceGroup {

    /**
     * 主数据源列表
     */
    private final List<DataSource> masterDataSourceList;

    /**
     * 从数据源列表
     */
    private final List<DataSource> slaveDataSourceList;

    /**
     * 是否有从数据源
     */
    private final boolean hasSlave;

    /**
     * 构造函数
     * @param masterDataSourceList 主数据源列表
     * @param slaveDataSourceList 从数据源列表
     */
    public DataSourceGroup(List<DataSource> masterDataSourceList, List<DataSource> slaveDataSourceList) {
        if (ObjectUtils.isEmpty(masterDataSourceList)) {
            throw new IllegalArgumentException("Master datasource list must not be empty.");
        }
        this.masterDataSourceList = Collections.unmodifiableList(masterDataSourceList);
        this.slaveDataSourceList = ObjectUtils.isEmpty(slaveDataSourceList)
            ? Collections.emptyList()
            : Collections.unmodifiableList(slaveDataSourceList);
        this.hasSlave = !this.slaveDataSourceList.isEmpty();
    }

    /**
     * 构造函数
     * @param masterDataSourceList 主数据源列表
     */
    public DataSourceGroup(List<DataSource> masterDataSourceList) {
        this(masterDataSourceList, Collections.emptyList());
    }

    /**
     * 根据读写类型选择数据源
     * @param isWriteOrTransaction 是否写操作或在事务中
     * @return DataSource
     */
    public DataSource select(boolean isWriteOrTransaction) {
        if (!hasSlave || isWriteOrTransaction) {
            return masterDataSourceList.get(ThreadLocalRandom.current().nextInt(masterDataSourceList.size()));
        }
        return slaveDataSourceList.get(ThreadLocalRandom.current().nextInt(slaveDataSourceList.size()));
    }

    /**
     * 获取主数据源列表
     * @return 主数据源列表
     */
    public List<DataSource> getMasterDataSourceList() {
        return masterDataSourceList;
    }

    /**
     * 获取从数据源列表
     * @return 从数据源列表
     */
    public List<DataSource> getSlaveDataSourceList() {
        return slaveDataSourceList;
    }

    /**
     * 是否有从数据源
     * @return 是否有从数据源
     */
    public boolean hasSlave() {
        return hasSlave;
    }
}
