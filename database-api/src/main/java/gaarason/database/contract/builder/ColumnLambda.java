package gaarason.database.contract.builder;

import gaarason.database.contract.eloquent.Builder;
import gaarason.database.contract.function.ColumnFunctionalInterface;

import java.util.Arrays;

/**
 * 列名
 * 仅用于 insert replace 两种语句
 * @param <T>
 * @param <K>
 * @author xt
 */
public interface ColumnLambda<T, K> extends Column<T, K>, Support<T, K> {

    /**
     * 新增字段
     * @param column 列名表达式
     * @return 查询构造器
     */
    default Builder<T, K> column(ColumnFunctionalInterface<T> column) {
        return column(lambda2ColumnName(column));
    }

    /**
     * 新增字段
     * @param column 列名表达式数组
     * @return 查询构造器
     */
    @SuppressWarnings("unchecked")
    default Builder<T, K> column(ColumnFunctionalInterface<T>... column) {
        return column(lambda2ColumnName(Arrays.asList(column)));
    }


}
