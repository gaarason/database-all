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
public interface GroupLambda<T, K> extends Group<T, K>, Support<T, K> {

    /**
     * 分组
     * @param column 列名表达式
     * @param <F> 属性类型
     * @return 查询构造器
     */
    default <F> Builder<T, K> group(ColumnFunctionalInterface<T, F> column) {
        return group(lambda2ColumnName(column));
    }

    /**
     * 分组
     * @param column 列名表达式数组
     * @param <F> 属性类型
     * @return 查询构造器
     */
    @SuppressWarnings("unchecked")
    default <F> Builder<T, K> group(ColumnFunctionalInterface<T, F>... column) {
        return group(lambda2ColumnName(Arrays.asList(column)));
    }


}
