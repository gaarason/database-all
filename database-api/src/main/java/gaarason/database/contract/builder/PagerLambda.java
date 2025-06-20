package gaarason.database.contract.builder;

import gaarason.database.appointment.CursorPaginate;
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
     * 光标分页
     * @param func 查询结果集转化 eg : FriendlyList::toObjectList
     * @param indexColumn 索引列名 (建议自增主键等)
     * @param indexValue 索引值 (当前光标位置, null 表示初始位置)
     * @param perPage 每页数量
     * @param hasTotal 是否查询总数
     * @return 光标分页对象
     * @param <V> 数据类型
     */
    default <V, F> CursorPaginate<V> cursorPaginate(RecordListConversionFunctionalInterface<T, K, V> func,
            ColumnFunctionalInterface<T, F> indexColumn, @Nullable Object indexValue, int perPage, boolean hasTotal) {
        return cursorPaginate(func, lambda2ColumnName(indexColumn), indexValue, perPage, hasTotal);
    }
}
