package gaarason.database.contract.builder;

import gaarason.database.appointment.CursorPaginate;
import gaarason.database.appointment.OrderBy;
import gaarason.database.appointment.PageNavigation;
import gaarason.database.contract.eloquent.Builder;
import gaarason.database.contract.function.ColumnFunctionalInterface;
import gaarason.database.contract.function.RecordListConversionFunctionalInterface;
import gaarason.database.lang.Nullable;

/**
 * 分页
 * @param <T>
 * @author xt
 */
public interface PagerLambda<B extends Builder<B, T, K>, T, K> extends Pager<B, T, K>, Support<B, T, K> {

    /**
     * 游标分页
     * 根据指定键, 查询下一页
     * @param indexColumn 索引列名 (建议自增主键等, 会加入查询列并进行排序)
     * @param nextIndex 游标位置, 用于查询下一页
     * @param perPage 每页数量
     * @return 游标分页对象
     * @param <F> 指定键的类型
     */
    default <F> CursorPaginate<T> cursorPaginate(ColumnFunctionalInterface<T, F> indexColumn, @Nullable Object nextIndex, int perPage) {
        return cursorPaginate(lambda2ColumnName(indexColumn), nextIndex, perPage);
    }

    /**
     * 游标分页
     * @param func 查询结果集转化 eg : FriendlyList::toObjectList
     * @param indexColumn 索引列名 (建议自增主键等, 会加入查询列并进行排序)
     * @param previousIndex 游标位置, 用于查询上一页 ( 可直接取上一次 cursorPaginate 对象的 previousIndex 属性 )
     * @param nextIndex 游标位置, 用于查询下一页 ( 可直接取上一次 cursorPaginate 对象的 nextIndex 属性 )
     * @param order 排序方式
     * @param pageNavigation 本次查询的是 下一页或者上一页
     * @param perPage 每页数量
     * @param hasTotal 是否查询总数
     * @return 游标分页对象
     * @param <V> 数据类型
     */
    default <V, F> CursorPaginate<V> cursorPaginate(RecordListConversionFunctionalInterface<T, K, V> func,
            ColumnFunctionalInterface<T, F> indexColumn, @Nullable Object previousIndex, @Nullable Object nextIndex,
            OrderBy order, PageNavigation pageNavigation, int perPage, boolean hasTotal) {
        return cursorPaginate(func, lambda2ColumnName(indexColumn), previousIndex, nextIndex, order, pageNavigation,
                perPage, hasTotal);
    }
}
