package gaarason.database.contract.routing;

import gaarason.database.lang.Nullable;

/**
 * 数据库键（注解/上下文同一维度）→ 最终用于 JDBC {@code setCatalog}/{@code setSchema} 的库键.
 * <p>
 * 可在实现内完成映射、trim、别名等逻辑。
 *
 * @author xt
 */
@FunctionalInterface
public interface DynamicDatabaseRouting {

    /**
     * 解析最终库键。
     *
     * @param databaseKey 数据库键，可为 {@code null}
     * @return 最终 JDBC catalog/schema 键，可为 {@code null}
     */
    @Nullable
    String resolvePhysical(@Nullable String databaseKey);
}
