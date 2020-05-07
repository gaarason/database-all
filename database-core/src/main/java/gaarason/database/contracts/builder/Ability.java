package gaarason.database.contracts.builder;

import gaarason.database.query.Builder;

/**
 * 能力
 * @param <T, K>
 */
public interface Ability<T, K> {

    /**
     * 随机抽样
     * 此方法大数据下表现良好
     * @param field 接收一个参数,默认为主键字段作为随机依据,当主键非常不均匀时应传入此字段(优先选用连续计数类型字段).
     * @return 查询构建器
     */
    Builder<T, K> inRandomOrder(String field);
}
