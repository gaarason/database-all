package gaarason.database.connection;

import gaarason.database.core.Container;
import gaarason.database.exception.TypeNotSupportedException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.List;
import java.util.Map;

/**
 * 路由数据源包装器(Spring 环境)
 * <p>
 * 基于 {@link GaarasonDataSourceContext} 进行数据源组路由, 结合 {@link DataSourceGroup} 实现读写分离.
 * 事务开始后锁定数据源组与解析后的库键, 保证事务期间不会因上下文切换而路由到其他组或其他库.
 *
 * @author xt
 */
public class GaarasonRoutingDataSourceWrapper extends GaarasonSmartDataSourceWrapper {

    private final Map<String, DataSourceGroup> groupMap;

    private final String defaultGroupKey;

    /**
     * 事务中锁定的数据源组 key, 保证事务期间不会因上下文切换而切到其他组
     */
    private final ThreadLocal<String> transactionGroupKey = new ThreadLocal<>();

    private final ThreadLocal<String> transactionDatabaseKey = new ThreadLocal<>();

    /**
     * 构造路由数据源包装器
     * @param groupMap 数据源组映射
     * @param defaultGroupKey 默认组名
     * @param container 容器
     */
    public GaarasonRoutingDataSourceWrapper(Map<String, DataSourceGroup> groupMap, String defaultGroupKey,
        Container container) {
        super(groupMap.get(defaultGroupKey).getMasterDataSourceList(), container);
        this.groupMap = groupMap;
        this.defaultGroupKey = defaultGroupKey;
    }

    @Override
    public void begin() {
        if (!isLocalThreadInTransaction()) {
            transactionGroupKey.set(GaarasonDataSourceContext.resolvePhysicalGroupKey(defaultGroupKey));
            transactionDatabaseKey.set(super.resolveDatabaseKeyForCurrentContext());
        }
        super.begin();
    }

    @Override
    protected DataSource getRealDataSource(boolean isWriteOrTransaction) {
        String key;
        if (isLocalThreadInTransaction()) {
            key = transactionGroupKey.get();
        } else {
            key = GaarasonDataSourceContext.resolvePhysicalGroupKey(defaultGroupKey);
        }
        DataSourceGroup group = groupMap.get(key);
        if (group == null) {
            throw new TypeNotSupportedException("Datasource group [" + key + "] not found.");
        }
        return group.select(isWriteOrTransaction);
    }

    @Override
    protected void connectionClose(Connection connection) {
        transactionGroupKey.remove();
        transactionDatabaseKey.remove();
        super.connectionClose(connection);
    }

    @Override
    protected String resolveDatabaseKeyForCurrentContext() {
        if (isLocalThreadInTransaction()) {
            return transactionDatabaseKey.get();
        }
        return super.resolveDatabaseKeyForCurrentContext();
    }

    @Override
    public List<DataSource> getMasterDataSourceList() {
        DataSourceGroup group = groupMap.get(GaarasonDataSourceContext.resolvePhysicalGroupKey(defaultGroupKey));
        return group != null ? group.getMasterDataSourceList() : super.getMasterDataSourceList();
    }

    @Override
    public List<DataSource> getSlaveDataSourceList() {
        DataSourceGroup group = groupMap.get(GaarasonDataSourceContext.resolvePhysicalGroupKey(defaultGroupKey));
        return group != null ? group.getSlaveDataSourceList() : super.getSlaveDataSourceList();
    }

    /**
     * 获取所有数据源组
     * @return 数据源组映射(不可变)
     */
    public Map<String, DataSourceGroup> getGroupMap() {
        return groupMap;
    }

    /**
     * 获取默认数据源组名
     * @return 默认组名
     */
    public String getDefaultGroupKey() {
        return defaultGroupKey;
    }
}
