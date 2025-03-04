package gaarason.database.contract.builder;

import gaarason.database.contract.eloquent.Builder;
import gaarason.database.contract.function.ColumnFunctionalInterface;

import java.util.Arrays;

/**
 * 分组
 * @param <T>
 * @param <K>
 * @author xt
 */
public interface GroupLambda<B extends Builder<B, T, K>, T, K> extends Group<B, T, K>, Support<B, T, K> {

    /**
     * 分组
     * @param column 列名表达式
     * @param <F> 属性类型
     * @return 查询构造器
     */
    default <F> B group(ColumnFunctionalInterface<T, F> column) {
        return group(lambda2ColumnName(column));
    }

    /**
     * 分组
     * @param column 列名表达式数组
     * @param <F> 属性类型
     * @return 查询构造器
     */
    @SuppressWarnings("unchecked")
    default <F> B group(ColumnFunctionalInterface<T, F>... column) {
        return group(lambda2ColumnName(Arrays.asList(column)));
    }


}
