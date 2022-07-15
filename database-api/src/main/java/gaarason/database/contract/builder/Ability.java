package gaarason.database.contract.builder;

import gaarason.database.contract.eloquent.Builder;

/**
 * 能力
 * @param <T>
 * @param <K>
 * @author xt
 */
public interface Ability<T, K> {

    /**
     * 随机抽样
     * 此方法大数据下表现良好
     * @return 查询构造器
     */
    Builder<T, K> inRandomOrder();

    /**
     * 随机抽样
     * 此方法大数据下表现良好
     * @param column 接收一个参数,默认为主键字段作为随机依据,当主键非常不均匀时应传入此字段(优先选用连续计数类型字段).
     * @return 查询构造器
     */
    Builder<T, K> inRandomOrder(String column);
}
