package gaarason.database.contract.routing;

import gaarason.database.lang.Nullable;

import javax.sql.DataSource;
import java.sql.Connection;

/**
 * 在已有 JDBC 连接上切换 catalog/schema（同连接多库），以及切库失败时的处理策略.
 *
 * @author xt
 */
@FunctionalInterface
public interface DynamicJdbcCatalogRouting {

    /**
     * 将库键应用到连接.
     *
     * @param dataSource 本次取连接所用的 {@link DataSource}（池实例），供实现按池缓存策略；可为 {@code null} 表示调用方未知
     * @param connection 连接
     * @param catalogKey 目标 catalog/库键
     * @throws Throwable 切换失败
     */
    void switchTo(@Nullable DataSource dataSource, Connection connection, String catalogKey) throws Throwable;

    /**
     * {@link #switchTo(DataSource, Connection, String)} 抛出异常时的处理；默认抛出带上下文的 {@link IllegalStateException}.
     *
     * @param connection 连接
     * @param catalogKey 目标库键
     * @param cause      切换失败原因
     */
    default void handleFailure(Connection connection, String catalogKey, Throwable cause) {
        throw new IllegalStateException(
            "Failed to switch database by key [" + catalogKey + "]: " + cause.getMessage(), cause);
    }
}
