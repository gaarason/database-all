package gaarason.database.contract.builder;

import gaarason.database.appointment.CursorPaginate;
import gaarason.database.appointment.OrderBy;
import gaarason.database.appointment.PageNavigation;
import gaarason.database.appointment.Paginate;
import gaarason.database.contract.eloquent.Builder;
import gaarason.database.contract.function.RecordListConversionFunctionalInterface;
import gaarason.database.contract.record.FriendlyList;
import gaarason.database.exception.SQLRuntimeException;
import gaarason.database.lang.Nullable;

import java.util.Map;

/**
 * 分页
 * @param <T>
 * @author xt
 */
public interface Pager<B extends Builder<B, T, K>, T, K> extends Support<B, T, K> {

    /**
     * 不包含总数的分页
     * @param currentPage 当前页
     * @param perPage 每页数量
     * @return 分页信息对象
     * @throws SQLRuntimeException sql错误
     */
    default Paginate<T> simplePaginate(int currentPage, int perPage) {
        return paginate(FriendlyList::toObjectList, currentPage, perPage, false);
    }

    /**
     * 不包含总数的分页
     * @param currentPage 当前页
     * @param perPage 每页数量
     * @return 分页信息对象
     * @throws SQLRuntimeException sql错误
     * @see #paginate(RecordListConversionFunctionalInterface, int, int, boolean)
     */
    @Deprecated
    default Paginate<T> simplePaginate(Object currentPage, Object perPage) throws SQLRuntimeException {
        return simplePaginate(conversionToInt(currentPage), conversionToInt(perPage));
    }

    /**
     * 不包含总数的分页
     * @param currentPage 当前页
     * @param perPage 每页数量
     * @return 分页信息对象
     * @throws SQLRuntimeException sql错误
     * @deprecated 使用 paginate(FriendlyList::toMapList, currentPage, perPage, false); 替代
     * @see #paginate(RecordListConversionFunctionalInterface, int, int, boolean)
     */
    @Deprecated
    default Paginate<Map<String, Object>> simplePaginateMapStyle(int currentPage, int perPage) throws SQLRuntimeException {
        return paginate(FriendlyList::toMapList, currentPage, perPage, false);
    }

    /**
     * 不包含总数的分页
     * @param currentPage 当前页
     * @param perPage 每页数量
     * @return 分页信息对象
     * @throws SQLRuntimeException sql错误
     * @see #paginate(RecordListConversionFunctionalInterface, int, int, boolean)
     */
    @Deprecated
    default Paginate<Map<String, Object>> simplePaginateMapStyle(Object currentPage, Object perPage)
        throws SQLRuntimeException {
        return simplePaginateMapStyle(conversionToInt(currentPage), conversionToInt(perPage));
    }

    /**
     * 包含总数的分页
     * @param currentPage 当前页
     * @param perPage 每页数量
     * @return 分页信息对象
     * @throws SQLRuntimeException sql错误
     */
    default Paginate<T> paginate(int currentPage, int perPage) throws SQLRuntimeException {
        return paginate(FriendlyList::toObjectList, currentPage, perPage, true);
    }

    /**
     * 包含总数的分页
     * @param currentPage 当前页
     * @param perPage 每页数量
     * @return 分页信息对象
     * @throws SQLRuntimeException sql错误
     * @see #paginate(RecordListConversionFunctionalInterface, int, int, boolean)
     */
    @Deprecated
    default Paginate<T> paginate(Object currentPage, Object perPage) throws SQLRuntimeException {
        return paginate(conversionToInt(currentPage), conversionToInt(perPage));
    }

    /**
     * 包含总数的分页
     * @param currentPage 当前页
     * @param perPage 每页数量
     * @return 分页信息对象
     * @throws SQLRuntimeException sql错误
     * @deprecated 使用 paginate(FriendlyList::toMapList, currentPage, perPage, true); 替代
     * @see #paginate(RecordListConversionFunctionalInterface, int, int, boolean)
     */
    @Deprecated
    default Paginate<Map<String, Object>> paginateMapStyle(int currentPage, int perPage) throws SQLRuntimeException {
        return paginate(FriendlyList::toMapList, currentPage, perPage, true);
    }

    /**
     * 包含总数的分页
     * @param currentPage 当前页
     * @param perPage 每页数量
     * @return 分页信息对象
     * @throws SQLRuntimeException sql错误
     * @see #paginate(RecordListConversionFunctionalInterface, int, int, boolean)
     */
    @Deprecated
    default Paginate<Map<String, Object>> paginateMapStyle(Object currentPage, Object perPage)
        throws SQLRuntimeException {
        return paginateMapStyle(conversionToInt(currentPage), conversionToInt(perPage));
    }

    /**
     * 偏移分页
     * @param func 查询结果集转化 eg : FriendlyList::toObjectList
     * @param currentPage 当前页
     * @param perPage 每页数量
     * @param hasTotal 是否查询总数
     * @return 偏移分页对象
     * @param <V> 数据类型
     */
    <V> Paginate<V> paginate(RecordListConversionFunctionalInterface<T, K, V> func, int currentPage, int perPage,
            boolean hasTotal);

    /**
     * 游标分页
     * 根据主键, 查询下一页
     * @param nextPrimaryKeyValue 主键值, 用于查询下一页
     * @param perPage 每页数量
     * @return 游标分页对象
     */
    CursorPaginate<T> cursorPaginate(@Nullable K nextPrimaryKeyValue, int perPage);

    /**
     * 游标分页
     * 根据指定键, 查询下一页
     * @param indexColumn 索引列名 (建议自增主键等, 会加入查询列并进行排序)
     * @param nextIndex 游标位置, 用于查询下一页
     * @param perPage 每页数量
     * @return 游标分页对象
     */
    default CursorPaginate<T> cursorPaginate(String indexColumn, @Nullable Object nextIndex, int perPage) {
        return cursorPaginate(FriendlyList::toObjectList, indexColumn, null, nextIndex, OrderBy.ASC,
                PageNavigation.NEXT, perPage, false);
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
    <V> CursorPaginate<V> cursorPaginate(RecordListConversionFunctionalInterface<T, K, V> func, String indexColumn,
            @Nullable Object previousIndex, @Nullable Object nextIndex, OrderBy order, PageNavigation pageNavigation,
            int perPage, boolean hasTotal);
}
