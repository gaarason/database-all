package gaarason.database.connection;

import gaarason.database.contract.routing.DynamicDatabaseRouting;
import gaarason.database.contract.routing.DynamicDataSourceGroupRouting;
import gaarason.database.contract.routing.DynamicExplicitTableRouting;
import gaarason.database.contract.routing.DynamicJdbcCatalogRouting;
import gaarason.database.contract.routing.DynamicTableRouting;
import gaarason.database.lang.Nullable;
import gaarason.database.util.ObjectUtils;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Supplier;

/**
 * Gaarason 数据源路由与动态表解析的<strong>线程级上下文</strong>入口.
 * <p>
 * 三维度（数据源组、库、表）在类内<strong>定义与使用方式对齐</strong>：
 * <ul>
 *   <li>线程栈：各维度均为 {@code push*} → {@code peekLogical*} → {@code pop*}（private）</li>
 *   <li>边界：各维度均为 {@code execute*} 入栈、执行业务、再出栈</li>
 *   <li>物理解析：显式逻辑入参为 {@code resolvePhysical*FromLogical(...)}（private）；结合栈顶对外的统一入口为
 *   {@link #resolvePhysicalGroupKey(String)}、{@link #resolvePhysicalDatabaseKey()}、{@link #resolvePhysicalTableName(String)}</li>
 * </ul>
 *
 * @author xt
 * @see GaarasonDataSourceWrapper
 */
public final class GaarasonDataSourceContext {

    private static final ThreadLocal<Deque<String>> GROUP_CONTEXT = ThreadLocal.withInitial(ArrayDeque::new);

    private static final ThreadLocal<Deque<String>> DATABASE_CONTEXT = ThreadLocal.withInitial(ArrayDeque::new);

    private static final ThreadLocal<Deque<String>> TABLE_CONTEXT = ThreadLocal.withInitial(ArrayDeque::new);

    private static volatile DynamicDataSourceGroupRouting dynamicDataSourceGroupRouting = groupKey -> groupKey;

    private static volatile DynamicDatabaseRouting dynamicDatabaseRouting = databaseKey -> databaseKey;

    private static volatile DynamicTableRouting dynamicTableRouting =
        (logicalTableName, routeExpression) -> {
            if (ObjectUtils.isEmpty(routeExpression)) {
                return logicalTableName;
            }
            return logicalTableName + "_" + routeExpression;
        };

    private static volatile DynamicJdbcCatalogRouting dynamicJdbcCatalogRouting =
        new DefaultDynamicJdbcCatalogRouting();

    private static volatile DynamicExplicitTableRouting dynamicExplicitTableRouting = () -> true;

    private GaarasonDataSourceContext() {
    }

    /**
     * 在数据源组路由上下文中执行代码,结束后弹出栈顶组键(与 {@link #executeDataSourceGroup(String, Runnable)} 语义一致).
     *
     * @param groupKey 逻辑组键,写入当前线程栈顶
     * @param supplier 业务逻辑
     * @param <T>      返回值类型
     * @return {@code supplier} 的返回值
     */
    public static <T> T executeDataSourceGroup(String groupKey, Supplier<T> supplier) {
        pushGroup(groupKey);
        try {
            return supplier.get();
        } finally {
            popGroup();
        }
    }

    /**
     * 在数据源组路由上下文中执行无返回值逻辑.
     *
     * @param groupKey 逻辑组键
     * @param runnable 业务逻辑
     */
    public static void executeDataSourceGroup(String groupKey, Runnable runnable) {
        executeDataSourceGroup(groupKey, () -> {
            runnable.run();
            return null;
        });
    }

    /**
     * 在数据库(库键)路由上下文中执行代码,供包装数据源在取连接后按 {@link DynamicJdbcCatalogRouting} 切换 catalog/schema.
     *
     * @param databaseKey 逻辑库键,写入当前线程栈顶
     * @param supplier      业务逻辑
     * @param <T>           返回值类型
     * @return {@code supplier} 的返回值
     */
    public static <T> T executeDatabase(String databaseKey, Supplier<T> supplier) {
        pushDatabase(databaseKey);
        try {
            return supplier.get();
        } finally {
            popDatabase();
        }
    }

    /**
     * 在数据库路由上下文中执行无返回值逻辑.
     *
     * @param databaseKey 逻辑库键
     * @param runnable      业务逻辑
     */
    public static void executeDatabase(String databaseKey, Runnable runnable) {
        executeDatabase(databaseKey, () -> {
            runnable.run();
            return null;
        });
    }

    /**
     * 在表路由表达式上下文中执行代码,与 {@link DynamicTableRouting} 配合将逻辑表名解析为物理表名.
     *
     * @param routeExpression 表路由表达式(栈顶),可为字面量或由切面写入的 SpEL 结果
     * @param supplier        业务逻辑
     * @param <T>             返回值类型
     * @return {@code supplier} 的返回值
     */
    public static <T> T executeTable(String routeExpression, Supplier<T> supplier) {
        pushTable(routeExpression);
        try {
            return supplier.get();
        } finally {
            popTable();
        }
    }

    /**
     * 在表路由上下文中执行无返回值逻辑.
     *
     * @param routeExpression 表路由表达式
     * @param runnable        业务逻辑
     */
    public static void executeTable(String routeExpression, Runnable runnable) {
        executeTable(routeExpression, () -> {
            runnable.run();
            return null;
        });
    }

    /**
     * 取栈顶逻辑组键,若无则使用默认键,再解析为物理组名(事务锁定快照、查 {@code groupMap} 前使用).
     *
     * @param defaultGroupKey 上下文无逻辑组时的默认组键
     * @return 物理组名
     */
    public static String resolvePhysicalGroupKey(String defaultGroupKey) {
        String logical = peekLogicalGroupKey();
        String key = logical != null ? logical : defaultGroupKey;
        return resolvePhysicalGroupKeyFromLogical(key);
    }

    /**
     * 取栈顶逻辑库键并解析为 JDBC 层 catalog/schema 键(数据源包装器解析当前连接库键时使用).
     *
     * @return 解析后的库键,栈空或解析结果为 null 时可为 {@code null}
     */
    @Nullable
    public static String resolvePhysicalDatabaseKey() {
        return resolvePhysicalDatabaseKeyFromLogical(peekLogicalDatabaseKey());
    }

    /**
     * 结合栈顶表路由表达式,将逻辑表名解析为物理表名.
     *
     * @param logicalTableName 逻辑表名(如实体 {@code @Table} 名)
     * @return 物理表名;解析为空时回退为逻辑表名
     */
    public static String resolvePhysicalTableName(String logicalTableName) {
        return resolvePhysicalTableNameFromLogical(logicalTableName, peekLogicalTableRoute());
    }

    /**
     * 动态表路由是否覆盖 Builder/原生 SQL 中显式指定的表名.
     *
     * @return {@code true} 时 {@link gaarason.database.query.AbstractBuilder} 等会对显式表名再作解析
     */
    public static boolean shouldDynamicTableOverrideExplicit() {
        return dynamicExplicitTableRouting.overridesExplicitTable();
    }

    /**
     * 注册库键解析策略(全局静态,通常在应用启动或构建 {@link GaarasonRoutingDataSourceBuilder} 时设置).
     */
    public static void setDynamicDatabaseRouting(DynamicDatabaseRouting routing) {
        dynamicDatabaseRouting = routing;
    }

    /**
     * 注册数据源组键解析策略.
     */
    public static void setDynamicDataSourceGroupRouting(DynamicDataSourceGroupRouting routing) {
        dynamicDataSourceGroupRouting = routing;
    }

    /**
     * 注册逻辑表名 → 物理表名解析策略.
     */
    public static void setDynamicTableRouting(DynamicTableRouting routing) {
        dynamicTableRouting = routing;
    }

    /**
     * 注册同连接切换 catalog/schema 的实现.
     */
    public static void setDynamicJdbcCatalogRouting(DynamicJdbcCatalogRouting routing) {
        dynamicJdbcCatalogRouting = routing;
    }

    /**
     * 注册「动态表是否覆盖显式表名」策略.
     */
    public static void setDynamicExplicitTableRouting(DynamicExplicitTableRouting routing) {
        dynamicExplicitTableRouting = routing;
    }

    /**
     * 获取当前生效的 JDBC catalog 路由实现(供数据源包装层在打开连接后调用).
     */
    public static DynamicJdbcCatalogRouting getDynamicJdbcCatalogRouting() {
        return dynamicJdbcCatalogRouting;
    }

    private static void pushGroup(String groupKey) {
        GROUP_CONTEXT.get().push(groupKey);
    }

    /**
     * 栈顶逻辑数据源组键；栈空为 {@code null}.
     */
    @Nullable
    private static String peekLogicalGroupKey() {
        Deque<String> stack = GROUP_CONTEXT.get();
        return stack.isEmpty() ? null : stack.peek();
    }

    private static void popGroup() {
        Deque<String> stack = GROUP_CONTEXT.get();
        if (!stack.isEmpty()) {
            stack.pop();
        }
        if (stack.isEmpty()) {
            GROUP_CONTEXT.remove();
        }
    }

    /**
     * 将显式逻辑组键解析为物理组名(不读线程栈).
     */
    private static String resolvePhysicalGroupKeyFromLogical(@Nullable String logicalGroupKey) {
        return dynamicDataSourceGroupRouting.resolvePhysical(logicalGroupKey);
    }

    private static void pushDatabase(String databaseKey) {
        DATABASE_CONTEXT.get().push(databaseKey);
    }

    /**
     * 栈顶逻辑数据库键；栈空为 {@code null}.
     */
    @Nullable
    private static String peekLogicalDatabaseKey() {
        Deque<String> stack = DATABASE_CONTEXT.get();
        return stack.isEmpty() ? null : stack.peek();
    }

    private static void popDatabase() {
        Deque<String> stack = DATABASE_CONTEXT.get();
        if (!stack.isEmpty()) {
            stack.pop();
        }
        if (stack.isEmpty()) {
            DATABASE_CONTEXT.remove();
        }
    }

    /**
     * 将显式逻辑库键解析为 JDBC 层 catalog/schema 键(不读线程栈).
     */
    @Nullable
    private static String resolvePhysicalDatabaseKeyFromLogical(@Nullable String logicalDatabaseKey) {
        return dynamicDatabaseRouting.resolvePhysical(logicalDatabaseKey);
    }

    private static void pushTable(String routeExpression) {
        TABLE_CONTEXT.get().push(routeExpression);
    }

    /**
     * 栈顶表路由表达式；栈空为 {@code null}.
     */
    @Nullable
    private static String peekLogicalTableRoute() {
        Deque<String> stack = TABLE_CONTEXT.get();
        return stack.isEmpty() ? null : stack.peek();
    }

    private static void popTable() {
        Deque<String> stack = TABLE_CONTEXT.get();
        if (!stack.isEmpty()) {
            stack.pop();
        }
        if (stack.isEmpty()) {
            TABLE_CONTEXT.remove();
        }
    }

    /**
     * 将逻辑表名与显式路由表达式解析为物理表名(不读线程栈上的路由).
     */
    private static String resolvePhysicalTableNameFromLogical(String logicalTableName, @Nullable String routeExpression) {
        String tableName = dynamicTableRouting.resolvePhysical(logicalTableName, routeExpression);
        return ObjectUtils.isEmpty(tableName) ? logicalTableName : tableName;
    }
}
