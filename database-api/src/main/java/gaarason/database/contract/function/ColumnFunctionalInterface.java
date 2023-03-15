package gaarason.database.contract.function;

import java.io.Serializable;
import java.util.Collection;
import java.util.function.Function;

/**
 * 使用 Lambda 风格的列名
 * @author xt
 * @param <T> 实体类型
 * @param <F> 字段类型
 */
@FunctionalInterface
public interface ColumnFunctionalInterface<T, F> extends Function<T, F>, Serializable {

    /**
     * 用于处理属性是 Collection 类型
     * @param <T> 实体类型
     * @param <E> 字段的关系类型 eg : 字段是 Set<AAA> , 那么字段的关系类型便是AAA
     */
    interface ColumnCollection<T, E> extends ColumnFunctionalInterface<T, Collection<E>> {}

    /**
     * 用于处理属性是 Collection 类型
     * @param <T> 实体类型
     * @param <E> 字段的关系类型 eg : 字段是 AAA[] , 那么字段的关系类型便是AAA
     */
    interface ColumnArray<T, E> extends ColumnFunctionalInterface<T, E[]> {}
}
