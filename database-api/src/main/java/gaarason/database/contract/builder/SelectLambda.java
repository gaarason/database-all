package gaarason.database.contract.builder;

import gaarason.database.contract.eloquent.Builder;
import gaarason.database.contract.function.ColumnFunctionalInterface;
import gaarason.database.contract.function.GenerateSqlPartFunctionalInterface;
import gaarason.database.lang.Nullable;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;

/**
 * 需求字段
 * @param <T>
 * @param <K>
 * @author xt
 */
public interface SelectLambda<T extends Serializable, K extends Serializable> extends Select<T, K>, Support<T, K> {

    /**
     * 查询字段
     * @param column 列名表达式
     * @return 查询构造器
     */
    default Builder<T, K> select(ColumnFunctionalInterface<T> column) {
        return select(lambda2ColumnName(column));
    }

    /**
     * 查询字段
     * @param column 列名表达式数组
     * @return 查询构造器
     */
    @SuppressWarnings("unchecked")
    default Builder<T, K> select(ColumnFunctionalInterface<T>... column) {
        return select(lambda2ColumnName(Arrays.asList(column)));

    }
}
