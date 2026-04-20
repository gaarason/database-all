package gaarason.database.contract.routing;

/**
 * 数据源组键（注解/上下文同一维度）→ groupMap 查找用的物理组名.
 *
 * @author xt
 */
@FunctionalInterface
public interface DynamicDataSourceGroupRouting {

    /**
     * 解析物理数据源组名.
     *
     * @param groupKey 组键
     * @return 物理组名
     */
    String resolvePhysical(String groupKey);
}
