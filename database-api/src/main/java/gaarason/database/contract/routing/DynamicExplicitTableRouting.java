package gaarason.database.contract.routing;

/**
 * 动态物理表解析是否覆盖 Builder 中显式 from/表名.
 * <p>
 * 用于在「手写表名」与「线程上下文表路由」之间二选一或并存: 返回 {@code false} 时保留显式表名,仅对未显式指定的场景走路由.
 *
 * @author xt
 */
@FunctionalInterface
public interface DynamicExplicitTableRouting {

    /**
     * @return {@code true} 覆盖显式表名；{@code false} 保留显式表名
     */
    boolean overridesExplicitTable();
}
