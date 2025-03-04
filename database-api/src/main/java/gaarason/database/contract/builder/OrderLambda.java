package gaarason.database.contract.builder;

import gaarason.database.contract.eloquent.Builder;
import gaarason.database.contract.function.ColumnFunctionalInterface;

/**
 * 排序
 * @param <T>
 * @param <K>
 * @author xt
 */
public interface OrderLambda<B extends Builder<B, T, K>, T, K> extends Order<B, T, K>, Support<B, T, K> {

    /**
     * 排序
     * @param column 列名表达式
     * @param orderByType 正序|倒序
     * @param <F> 属性类型
     * @return 查询构造器
     */
    default <F> B orderBy(ColumnFunctionalInterface<T, F> column,
        gaarason.database.appointment.OrderBy orderByType) {
        return orderBy(lambda2ColumnName(column), orderByType);
    }

    /**
     * 排序
     * @param column 列名表达式
     * @param <F> 属性类型
     * @return 查询构造器
     */
    default <F> B orderBy(ColumnFunctionalInterface<T, F> column) {
        return orderBy(lambda2ColumnName(column));
    }
}
