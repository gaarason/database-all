package gaarason.database.contracts.builder;

/**
 * 统计
 * @param <T>
 * @param <K>
 */
public interface Aggregates<T, K> {

    /**
     * count 条数统计,兼容 group
     * @param column 统计字段
     * @return 计数
     */
    Long count(String column);

    /**
     * 求最大值
     * @param column 统计字段
     * @return 最大值
     */
    String max(String column);

    /**
     * 求最小值
     * @param column 统计字段
     * @return 最小值
     */
    String min(String column);

    /**
     * 求平均值
     * @param column 统计字段
     * @return 平均值
     */
    String avg(String column);

    /**
     * 求和
     * @param column 统计字段
     * @return 总和
     */
    String sum(String column);
}
