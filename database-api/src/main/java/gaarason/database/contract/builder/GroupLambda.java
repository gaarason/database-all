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
     * @return 查询构造器
     */
    default Builder<T, K> group(ColumnFunctionalInterface<T> column) {
        return group(lambda2ColumnName(column));
    }

    /**
     * 分组
     * @param column 列名表达式数组
     * @return 查询构造器
     */
    @SuppressWarnings("unchecked")
    default Builder<T, K> group(ColumnFunctionalInterface<T>... column) {
        return group(lambda2ColumnName(Arrays.asList(column)));
    }


}
