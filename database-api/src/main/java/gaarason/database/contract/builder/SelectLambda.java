package gaarason.database.contract.builder;

import gaarason.database.contract.eloquent.Builder;
import gaarason.database.contract.function.ColumnFunctionalInterface;

import java.util.Arrays;

/**
 * 需求字段
 * @param <T>
 * @param <K>
 * @author xt
 */
public interface SelectLambda<B extends Builder<B, T, K>, T, K> extends Select<B, T, K>, Support<B, T, K> {

    /**
     * 查询字段
     * @param column 列名表达式
     * @param <F> 属性类型
     * @return 查询构造器
     */
    default <F> B select(ColumnFunctionalInterface<T, F> column) {
        return select(lambda2ColumnName(column));
    }

    /**
     * 查询字段
     * @param column 列名表达式数组
     * @param <F> 属性类型
     * @return 查询构造器
     */
    @SuppressWarnings("unchecked")
    default <F> B select(ColumnFunctionalInterface<T, F>... column) {
        return select(lambda2ColumnName(Arrays.asList(column)));

    }
}
