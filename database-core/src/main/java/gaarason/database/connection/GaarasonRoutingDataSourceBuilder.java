package gaarason.database.connection;

import gaarason.database.contract.connection.GaarasonDataSource;
import gaarason.database.core.Container;
import gaarason.database.exception.TypeNotSupportedException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 路由数据源构建器(不依赖 Spring)
 * <p>
 * 使用示例:
 * <pre>{@code
 * GaarasonDataSource ds = GaarasonRoutingDataSourceBuilder.create()
 *     .defaultGroup("master")
 *     .group("master", masterDataSources, slaveDataSources)
 *     .group("order", orderDataSources)
 *     .build(container);
 * }</pre>
 * @author xt
 */
public class GaarasonRoutingDataSourceBuilder {

    private String defaultGroupKey = "master";

    private final Map<String, DataSourceGroup> groupMap = new LinkedHashMap<>();

    private GaarasonRoutingDataSourceBuilder() {
    }

    /**
     * 创建构建器
     * @return 构建器实例
     */
    public static GaarasonRoutingDataSourceBuilder create() {
        return new GaarasonRoutingDataSourceBuilder();
    }

    /**
     * 设置默认数据源组名
     * @param groupKey 组名
     * @return 构建器
     */
    public GaarasonRoutingDataSourceBuilder defaultGroup(String groupKey) {
        this.defaultGroupKey = groupKey;
        return this;
    }

    /**
     * 添加一个数据源组(含读写分离)
     * @param key 组名
     * @param masterDataSources 主数据源列表(写)
     * @param slaveDataSources 从数据源列表(读)
     * @return 构建器
     */
    public GaarasonRoutingDataSourceBuilder group(String key, List<DataSource> masterDataSources,
        List<DataSource> slaveDataSources) {
        groupMap.put(key, new DataSourceGroup(masterDataSources, slaveDataSources));
        return this;
    }

    /**
     * 添加一个数据源组(仅主库)
     * @param key 组名
     * @param masterDataSources 主数据源列表
     * @return 构建器
     */
    public GaarasonRoutingDataSourceBuilder group(String key, List<DataSource> masterDataSources) {
        groupMap.put(key, new DataSourceGroup(masterDataSources));
        return this;
    }

    /**
     * 添加一个数据源组
     * @param key 组名
     * @param dataSourceGroup 数据源组
     * @return 构建器
     */
    public GaarasonRoutingDataSourceBuilder group(String key, DataSourceGroup dataSourceGroup) {
        groupMap.put(key, dataSourceGroup);
        return this;
    }

    /**
     * 构建路由数据源
     * @param container 容器
     * @return GaarasonDataSource
     */
    public GaarasonDataSource build(Container container) {
        if (groupMap.isEmpty()) {
            throw new IllegalArgumentException("At least one datasource group must be configured.");
        }
        if (!groupMap.containsKey(defaultGroupKey)) {
            throw new IllegalArgumentException(
                "Default group [" + defaultGroupKey + "] not found in configured groups.");
        }
        return new RoutingDataSourceWrapper(groupMap, defaultGroupKey, container);
    }

    /**
     * 路由数据源包装器(不依赖 Spring)
     */
    static class RoutingDataSourceWrapper extends GaarasonDataSourceWrapper {

        private final Map<String, DataSourceGroup> groupMap;

        private final String defaultGroupKey;

        /**
         * 事务中锁定的数据源组 key, 保证事务期间不会因上下文切换而切到其他组
         */
        private final ThreadLocal<String> transactionGroupKey = new ThreadLocal<>();

        RoutingDataSourceWrapper(Map<String, DataSourceGroup> groupMap, String defaultGroupKey, Container container) {
            super(groupMap.get(defaultGroupKey).getMasterDataSourceList(), container);
            this.groupMap = groupMap;
            this.defaultGroupKey = defaultGroupKey;
        }

        @Override
        public void begin() {
            if (!isLocalThreadInTransaction()) {
                transactionGroupKey.set(resolveGroupKey());
            }
            super.begin();
        }

        @Override
        protected DataSource getRealDataSource(boolean isWriteOrTransaction) {
            String key;
            if (isLocalThreadInTransaction()) {
                key = transactionGroupKey.get();
            } else {
                key = resolveGroupKey();
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
            super.connectionClose(connection);
        }

        @Override
        public List<DataSource> getMasterDataSourceList() {
            DataSourceGroup group = groupMap.get(resolveGroupKey());
            return group != null ? group.getMasterDataSourceList() : super.getMasterDataSourceList();
        }

        @Override
        public List<DataSource> getSlaveDataSourceList() {
            DataSourceGroup group = groupMap.get(resolveGroupKey());
            return group != null ? group.getSlaveDataSourceList() : super.getSlaveDataSourceList();
        }

        private String resolveGroupKey() {
            String key = GaarasonDataSourceContext.get();
            return key != null ? key : defaultGroupKey;
        }
    }
}
