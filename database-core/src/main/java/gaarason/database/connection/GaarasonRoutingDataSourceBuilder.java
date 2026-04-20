package gaarason.database.connection;

import gaarason.database.contract.connection.GaarasonDataSource;
import gaarason.database.contract.routing.DynamicDatabaseRouting;
import gaarason.database.contract.routing.DynamicDataSourceGroupRouting;
import gaarason.database.contract.routing.DynamicExplicitTableRouting;
import gaarason.database.contract.routing.DynamicJdbcCatalogRouting;
import gaarason.database.contract.routing.DynamicTableRouting;
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

    private DynamicDatabaseRouting dynamicDatabaseRouting;

    private DynamicDataSourceGroupRouting dynamicDataSourceGroupRouting;

    private DynamicTableRouting dynamicTableRouting;

    private DynamicJdbcCatalogRouting dynamicJdbcCatalogRouting;

    private DynamicExplicitTableRouting dynamicExplicitTableRouting;

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
     * 指定库键解析策略;构建时写入 {@link GaarasonDataSourceContext}.
     *
     * @param routing 库键路由,可 {@code null} 表示沿用上下文已有配置
     * @return 当前构建器
     */
    public GaarasonRoutingDataSourceBuilder dynamicDatabaseRouting(DynamicDatabaseRouting routing) {
        this.dynamicDatabaseRouting = routing;
        return this;
    }

    /**
     * 指定数据源组键解析策略.
     *
     * @param routing 组键路由
     * @return 当前构建器
     */
    public GaarasonRoutingDataSourceBuilder dynamicDataSourceGroupRouting(DynamicDataSourceGroupRouting routing) {
        this.dynamicDataSourceGroupRouting = routing;
        return this;
    }

    /**
     * 指定逻辑表名解析策略.
     *
     * @param routing 表路由
     * @return 当前构建器
     */
    public GaarasonRoutingDataSourceBuilder dynamicTableRouting(DynamicTableRouting routing) {
        this.dynamicTableRouting = routing;
        return this;
    }

    /**
     * 指定同连接切换 catalog/schema 的实现.
     *
     * @param routing JDBC catalog 路由
     * @return 当前构建器
     */
    public GaarasonRoutingDataSourceBuilder dynamicJdbcCatalogRouting(DynamicJdbcCatalogRouting routing) {
        this.dynamicJdbcCatalogRouting = routing;
        return this;
    }

    /**
     * 指定动态表是否覆盖显式 {@code from}/表名.
     *
     * @param routing 显式表覆盖策略
     * @return 当前构建器
     */
    public GaarasonRoutingDataSourceBuilder dynamicExplicitTableRouting(DynamicExplicitTableRouting routing) {
        this.dynamicExplicitTableRouting = routing;
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
        applyRoutingExtensions();
        return new RoutingDataSourceWrapper(groupMap, defaultGroupKey, container);
    }

    /**
     * 将构建器上可选的路由扩展写入 {@link GaarasonDataSourceContext} 全局静态配置.
     */
    private void applyRoutingExtensions() {
        if (dynamicDatabaseRouting != null) {
            GaarasonDataSourceContext.setDynamicDatabaseRouting(dynamicDatabaseRouting);
        }
        if (dynamicDataSourceGroupRouting != null) {
            GaarasonDataSourceContext.setDynamicDataSourceGroupRouting(dynamicDataSourceGroupRouting);
        }
        if (dynamicTableRouting != null) {
            GaarasonDataSourceContext.setDynamicTableRouting(dynamicTableRouting);
        }
        if (dynamicJdbcCatalogRouting != null) {
            GaarasonDataSourceContext.setDynamicJdbcCatalogRouting(dynamicJdbcCatalogRouting);
        }
        if (dynamicExplicitTableRouting != null) {
            GaarasonDataSourceContext.setDynamicExplicitTableRouting(dynamicExplicitTableRouting);
        }
    }

    /**
     * 非 Spring 环境下的多组路由数据源实现；事务内锁定组键与库键,避免嵌套执行期间路由漂移.
     */
    static class RoutingDataSourceWrapper extends GaarasonDataSourceWrapper {

        private final Map<String, DataSourceGroup> groupMap;

        private final String defaultGroupKey;

        /**
         * 事务中锁定的数据源组 key, 保证事务期间不会因上下文切换而切到其他组
         */
        private final ThreadLocal<String> transactionGroupKey = new ThreadLocal<>();

        private final ThreadLocal<String> transactionDatabaseKey = new ThreadLocal<>();

        RoutingDataSourceWrapper(Map<String, DataSourceGroup> groupMap, String defaultGroupKey, Container container) {
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
    }
}
