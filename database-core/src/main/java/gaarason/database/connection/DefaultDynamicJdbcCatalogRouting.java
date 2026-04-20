package gaarason.database.connection;

import gaarason.database.contract.routing.DynamicJdbcCatalogRouting;
import gaarason.database.lang.Nullable;
import gaarason.database.util.ObjectUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 默认同连接切库：优先 {@link Connection#setCatalog}，失败则回退 {@link Connection#setSchema}。
 * <p>
 * 当调用方传入非空 {@link DataSource} 时，以 {@link System#identityHashCode(Object)} 区分池实例并缓存策略，
 * 热路径不再访问 {@link java.sql.DatabaseMetaData}；{@code dataSource == null} 时每次按 catalog→schema 探测、不参与缓存。
 *
 * @author xt
 */
final class DefaultDynamicJdbcCatalogRouting implements DynamicJdbcCatalogRouting {

    private enum SwitchMode {
        CATALOG,
        SCHEMA
    }

    private static final ConcurrentHashMap<String, SwitchMode> MODE_BY_POOL_KEY = new ConcurrentHashMap<>();

    private static final Object UNKNOWN_RESOLUTION_LOCK = new Object();

    @Override
    public void switchTo(@Nullable DataSource dataSource, Connection connection, String catalogKey) throws Throwable {
        if (ObjectUtils.isEmpty(catalogKey)) {
            return;
        }
        if (dataSource == null) {
            uncachedCatalogThenSchema(connection, catalogKey);
            return;
        }
        String poolKey = poolKey(dataSource);
        SwitchMode mode = MODE_BY_POOL_KEY.get(poolKey);
        if (mode == SwitchMode.SCHEMA) {
            connection.setSchema(catalogKey);
            return;
        }
        if (mode == SwitchMode.CATALOG) {
            connection.setCatalog(catalogKey);
            return;
        }
        synchronized (UNKNOWN_RESOLUTION_LOCK) {
            mode = MODE_BY_POOL_KEY.get(poolKey);
            if (mode == SwitchMode.SCHEMA) {
                connection.setSchema(catalogKey);
                return;
            }
            if (mode == SwitchMode.CATALOG) {
                connection.setCatalog(catalogKey);
                return;
            }
            try {
                connection.setCatalog(catalogKey);
                MODE_BY_POOL_KEY.put(poolKey, SwitchMode.CATALOG);
            } catch (SQLException first) {
                try {
                    connection.setSchema(catalogKey);
                    MODE_BY_POOL_KEY.put(poolKey, SwitchMode.SCHEMA);
                } catch (SQLException second) {
                    first.addSuppressed(second);
                    throw first;
                }
            }
        }
    }

    private static String poolKey(DataSource dataSource) {
        return "ds:" + System.identityHashCode(dataSource);
    }

    private static void uncachedCatalogThenSchema(Connection connection, String catalogKey) throws Throwable {
        try {
            connection.setCatalog(catalogKey);
        } catch (SQLException first) {
            connection.setSchema(catalogKey);
        }
    }
}
