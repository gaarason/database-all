package gaarason.database.contract.builder;

import gaarason.database.contract.eloquent.Builder;
import gaarason.database.contract.function.ColumnFunctionalInterface;

/**
 * 能力
 * @param <T>
 * @param <K>
 * @author xt
 */
public interface AbilityLambda<B extends Builder<B, T, K>, T, K> extends Ability<B, T, K>, Support<B, T, K> {

    /**
     * 随机抽样
     * 此方法大数据下表现良好
     * @param column 接收一个参数,默认为主键字段作为随机依据,当主键非常不均匀时应传入此字段(优先选用连续计数类型字段).
     * @param <F> 属性类型
     * @return 查询构造器
     */
    default <F> B inRandomOrder(ColumnFunctionalInterface<T, F> column) {
        return inRandomOrder(lambda2ColumnName(column));
    }

}
