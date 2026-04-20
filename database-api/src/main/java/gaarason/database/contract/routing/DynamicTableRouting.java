package gaarason.database.contract.routing;

import gaarason.database.lang.Nullable;

/**
 * 逻辑表名 + 表路由表达式 → 物理表名.
 *
 * @author xt
 */
@FunctionalInterface
public interface DynamicTableRouting {

    /**
     * 解析物理表名.
     *
     * @param logicalTableName 逻辑表名
     * @param routeExpression  表路由表达式，可为 {@code null}
     * @return 物理表名
     */
    String resolvePhysical(String logicalTableName, @Nullable String routeExpression);
}
