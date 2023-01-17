package gaarason.database.contract.builder;

import gaarason.database.contract.eloquent.Builder;
import gaarason.database.contract.function.ColumnFunctionalInterface;

/**
 * 排序
 * @param <T>
 * @param <K>
 * @author xt
 */
public interface OrderLambda<T, K> extends Order<T, K>, Support<T, K> {

    /**
     * 排序
     * @param column 列名表达式
     * @param orderByType 正序|倒序
     * @return 查询构造器
     */
    default Builder<T, K> orderBy(ColumnFunctionalInterface<T> column,
        gaarason.database.appointment.OrderBy orderByType) {
        return orderBy(lambda2ColumnName(column), orderByType);
    }

    /**
     * 排序
     * @param column 列名表达式
     * @return 查询构造器
     */
    default Builder<T, K> orderBy(ColumnFunctionalInterface<T> column) {
        return orderBy(lambda2ColumnName(column));
    }
}
