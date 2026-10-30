package gaarason.database.connection;

import gaarason.database.core.Container;
import gaarason.database.exception.TypeNotSupportedException;
import gaarason.database.logging.Log;
import gaarason.database.logging.LogFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 路由数据源包装器(Spring 环境)
 * <p>
 * 基于 {@link GaarasonDataSourceContext} 进行数据源组路由, 结合 {@link DataSourceGroup} 实现读写分离.
 * 事务开始后锁定数据源组与解析后的库键, 保证事务期间不会因上下文切换而路由到其他组或其他库.
 *
 * @author xt
 */
public class GaarasonRoutingDataSourceWrapper extends GaarasonSmartDataSourceWrapper {

    private static final Log LOGGER = LogFactory.getLog(GaarasonRoutingDataSourceWrapper.class);

    private final Map<String, DataSourceGroup> groupMap;

    private final String defaultGroupKey;

    /**
     * 由自动配置自建的连接池(不在 Spring 容器内, 必须随本包装器一起关闭)
     */
    private final List<DataSource> createdDataSourceList;

    /**
     * 是否已关闭, 保证重复关闭幂等
     */
    private final AtomicBoolean closed = new AtomicBoolean(false);

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
        this(groupMap, defaultGroupKey, container, Collections.emptyList());
    }

    /**
     * 构造路由数据源包装器
     * @param groupMap 数据源组映射
     * @param defaultGroupKey 默认组名
     * @param container 容器
     * @param createdDataSourceList 由自动配置自建的连接池, Spring 托管的 DataSource 不得列入
     */
    public GaarasonRoutingDataSourceWrapper(Map<String, DataSourceGroup> groupMap, String defaultGroupKey,
        Container container, List<DataSource> createdDataSourceList) {
        super(groupMap.get(defaultGroupKey).getMasterDataSourceList(), container);
        this.groupMap = groupMap;
        this.defaultGroupKey = defaultGroupKey;
        this.createdDataSourceList = new ArrayList<>();
        if (createdDataSourceList != null) {
            this.createdDataSourceList.addAll(createdDataSourceList);
        }
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

    /**
     * 释放由自动配置自建的连接池.
     * <p>
     * Spring 托管的 DataSource 由其自身生命周期关闭, 此处不越权关闭; 单个连接池关闭失败不影响其余连接池的释放.
     * 上下文关闭时由 Spring 推断的 destroy 方法自动调用.
     */
    public void close() {
        if (!closed.compareAndSet(false, true)) {
            return;
        }
        for (DataSource dataSource : createdDataSourceList) {
            if (!(dataSource instanceof AutoCloseable)) {
                continue;
            }
            try {
                ((AutoCloseable) dataSource).close();
            } catch (Throwable e) {
                LOGGER.warn("Failed to close self-created datasource [" + dataSource + "]: " + e.getMessage());
            }
        }
    }
}
