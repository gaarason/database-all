package gaarason.database.contract.builder;

import gaarason.database.contract.eloquent.Builder;
import gaarason.database.contract.function.ColumnFunctionalInterface;
import gaarason.database.contract.function.GenerateSqlPartFunctionalInterface;
import gaarason.database.lang.Nullable;

import java.util.Arrays;
import java.util.Collection;

/**
 * 条件
 * @param <T>
 * @param <K>
 * @author xt
 */
public interface HavingLambda<T, K> extends Having<T, K>, Support<T, K> {

    /**
     * 比较列与值
     * @param column 列名表达式
     * @param symbol 比较关系
     * @param value 值
     * @return 查询构造器
     */
    default Builder<T, K> having(ColumnFunctionalInterface<T> column, String symbol, Object value) {
        return having(lambda2ColumnName(column), symbol, value);
    }

    /**
     * 比较列与值(忽略值为null的情况)
     * @param column 列名表达式
     * @param symbol 比较关系
     * @param value 值
     * @return 查询构造器
     */
    default Builder<T, K> havingIgnoreNull(ColumnFunctionalInterface<T> column, String symbol, @Nullable Object value) {
        return havingIgnoreNull(lambda2ColumnName(column), symbol, value);
    }

    /**
     * 比较列与值相等
     * @param column 列名表达式
     * @param value 值
     * @return 查询构造器
     */
    default Builder<T, K> having(ColumnFunctionalInterface<T> column, @Nullable Object value) {
        return having(lambda2ColumnName(column), value);
    }

    /**
     * 比较列与值相等(忽略值为null的情况)
     * @param column 列名表达式
     * @param value 值
     * @return 查询构造器
     */
    default Builder<T, K> havingIgnoreNull(ColumnFunctionalInterface<T> column, @Nullable Object value) {
        return havingIgnoreNull(lambda2ColumnName(column), value);
    }

    /**
     * 在多个列中, 查找值
     * 当 value 以 %开头或者结尾时, 使用like查询
     * 当 value 为 null 时, 使用 is null 查询
     * 其他情况下, 使用 = 查询
     * @param value 值
     * @param columns 多个列名表达式
     * @return 查询构造器
     */
    @SuppressWarnings("unchecked")
    default Builder<T, K> havingKeywords(@Nullable Object value, ColumnFunctionalInterface<T>... columns) {
        return havingKeywords(value, lambda2ColumnName(Arrays.asList(columns)));
    }

    /**
     * 在多个列中, 查找值
     * 当 value 以 %开头或者结尾时, 使用like查询
     * 当 value 为 null 时, 忽略
     * 其他情况下, 使用 = 查询
     * @param value 值
     * @param columns 多个列名表达式
     * @return 查询构造器
     */
    @SuppressWarnings("unchecked")
    default Builder<T, K> havingKeywordsIgnoreNull(@Nullable Object value, ColumnFunctionalInterface<T>... columns) {
        return havingKeywordsIgnoreNull(value, lambda2ColumnName(Arrays.asList(columns)));
    }

    /**
     * "列like值" 的查询条件
     * 其中值如果没有在开头或结尾自行包含 % 符号，则在开头以及结尾拼接 % 符号
     * 忽略值为null的情况
     * 忽略值为 % 、%%的情况
     * @param column 列名表达式
     * @param value 值
     * @return 查询构造器
     */
    default Builder<T, K> havingLike(ColumnFunctionalInterface<T> column, @Nullable Object value) {
        return havingLike(lambda2ColumnName(column), value);
    }

    /**
     * "列 not like值" 的查询条件
     * 其中值如果没有在开头或结尾自行包含 % 符号，则在开头以及结尾拼接 % 符号
     * 忽略值为null的情况
     * 忽略值为 % 、%%的情况
     * @param column 列名表达式
     * @param value 值
     * @return 查询构造器
     */
    default Builder<T, K> havingNotLike(ColumnFunctionalInterface<T> column, @Nullable Object value) {
        return havingNotLike(lambda2ColumnName(column), value);
    }

    /**
     * 选择可能的条件类型
     * 当 value 以 %开头或者结尾时, 使用like查询
     * 当 value 为 null 时, 使用 is null 查询
     * 其他情况下, 使用 = 查询
     * @param column 列名表达式
     * @param value 值
     * @return 查询构造器
     */
    default Builder<T, K> havingMayLike(ColumnFunctionalInterface<T> column, @Nullable Object value) {
        return havingMayLike(lambda2ColumnName(column), value);
    }

    /**
     * 选择可能的条件类型
     * 当 value 以 %开头或者结尾时, 使用like查询
     * 当 value 为 null 时, 使用 is null 查询
     * 其他情况下, 使用 = 查询
     * @param column 列名表达式
     * @param value 值
     * @return 查询构造器
     */
    default Builder<T, K> havingMayNotLike(ColumnFunctionalInterface<T> column, @Nullable Object value) {
        return havingMayNotLike(lambda2ColumnName(column), value);
    }

    /**
     * 选择可能的条件类型
     * 当 value 以 %开头或者结尾时, 使用like查询
     * 当 value 为 null 时, 忽略
     * 其他情况下, 使用 = 查询
     * @param column 列名表达式
     * @param value 值
     * @return 查询构造器
     */
    default Builder<T, K> havingMayLikeIgnoreNull(ColumnFunctionalInterface<T> column, @Nullable Object value) {
        return havingMayLikeIgnoreNull(lambda2ColumnName(column), value);
    }

    /**
     * 选择可能的条件类型
     * 当 value 以 %开头或者结尾时, 使用not like查询
     * 当 value 为 null 时, 忽略
     * 其他情况下, 使用 != 查询
     * @param column 列名
     * @param value 值
     * @return 查询构造器
     */
    default Builder<T, K> havingMayNotLikeIgnoreNull(ColumnFunctionalInterface<T> column, @Nullable Object value) {
        return havingMayNotLikeIgnoreNull(lambda2ColumnName(column), value);
    }

    /**
     * 条件子查询
     * @param column 列名表达式
     * @param symbol 关系符号
     * @param completeSql 完整sql
     * @return 查询构造器
     */
    default Builder<T, K> havingSubQuery(ColumnFunctionalInterface<T> column, String symbol, String completeSql) {
        return havingSubQuery(lambda2ColumnName(column), symbol, completeSql);
    }

    /**
     * 条件子查询
     * @param column 列名表达式
     * @param symbol 关系符号
     * @param closure 闭包
     * @return 查询构造器
     */
    default Builder<T, K> havingSubQuery(ColumnFunctionalInterface<T> column, String symbol,
        GenerateSqlPartFunctionalInterface<T, K> closure) {
        return havingSubQuery(lambda2ColumnName(column), symbol, closure);
    }

    /**
     * 列值在范围内
     * @param column 列名表达式
     * @param valueList 值所在的list
     * @return 查询构造器
     */
    default Builder<T, K> havingIn(ColumnFunctionalInterface<T> column, Collection<?> valueList) {
        return havingIn(lambda2ColumnName(column), valueList);
    }

    /**
     * 列值在范围内(忽略值为空的情况)
     * @param column 列名表达式
     * @param valueList 值所在的list
     * @return 查询构造器
     */
    default Builder<T, K> havingInIgnoreEmpty(ColumnFunctionalInterface<T> column, @Nullable Collection<?> valueList) {
        return havingInIgnoreEmpty(lambda2ColumnName(column), valueList);
    }

    /**
     * 列值在范围内
     * @param column 列名表达式
     * @param valueArray 值所在的数组
     * @return 查询构造器
     */
    default Builder<T, K> havingIn(ColumnFunctionalInterface<T> column, Object... valueArray) {
        return havingIn(lambda2ColumnName(column), valueArray);
    }

    /**
     * 列值在范围内(忽略值为空的情况)
     * @param column 列名表达式
     * @param valueArray 值所在的数组
     * @return 查询构造器
     */
    default Builder<T, K> havingInIgnoreEmpty(ColumnFunctionalInterface<T> column, @Nullable Object... valueArray) {
        return havingInIgnoreEmpty(lambda2ColumnName(column), valueArray);
    }

    /**
     * 列值在范围内(子查询)
     * @param column 列名表达式
     * @param sql 完整sql eg:select id from student having age>10
     * @return 查询构造器
     */
    default Builder<T, K> havingInRaw(ColumnFunctionalInterface<T> column, String sql) {
        return havingInRaw(lambda2ColumnName(column), sql);
    }

    /**
     * 列值在范围内(子查询)
     * @param column 列名表达式
     * @param closure 闭包
     * @return 查询构造器
     */
    default Builder<T, K> havingIn(ColumnFunctionalInterface<T> column,
        GenerateSqlPartFunctionalInterface<T, K> closure) {
        return havingIn(lambda2ColumnName(column), closure);
    }

    /**
     * 列值不在范围内
     * @param column 列名表达式
     * @param valueList 值所在的list
     * @return 查询构造器
     */
    default Builder<T, K> havingNotIn(ColumnFunctionalInterface<T> column, Collection<?> valueList) {
        return havingNotIn(lambda2ColumnName(column), valueList);
    }

    /**
     * 列值在范围内(忽略值为空的情况)
     * @param column 列名表达式
     * @param valueList 值所在的list
     * @return 查询构造器
     */
    default Builder<T, K> havingNotInIgnoreEmpty(ColumnFunctionalInterface<T> column,
        @Nullable Collection<?> valueList) {
        return havingNotInIgnoreEmpty(lambda2ColumnName(column), valueList);
    }

    /**
     * 列值在范围内
     * @param column 列名表达式
     * @param valueArray 值所在的数组
     * @return 查询构造器
     */
    default Builder<T, K> havingNotIn(ColumnFunctionalInterface<T> column, Object... valueArray) {
        return havingNotIn(lambda2ColumnName(column), valueArray);
    }

    /**
     * 列值在范围内(忽略值为空的情况)
     * @param column 列名表达式
     * @param valueArray 值所在的数组
     * @return 查询构造器
     */
    default Builder<T, K> havingNotInIgnoreEmpty(ColumnFunctionalInterface<T> column, @Nullable Object... valueArray) {
        return havingNotInIgnoreEmpty(lambda2ColumnName(column), valueArray);
    }

    /**
     * 列值不在范围内(子查询)
     * @param column 列名表达式
     * @param sql 完整sql eg:select id from student having age>10
     * @return 查询构造器
     */
    default Builder<T, K> havingNotInRaw(ColumnFunctionalInterface<T> column, String sql) {
        return havingNotInRaw(lambda2ColumnName(column), sql);
    }

    /**
     * 列值不在范围内(子查询)
     * @param column 列名表达式
     * @param closure 闭包
     * @return 查询构造器
     */
    default Builder<T, K> havingNotIn(ColumnFunctionalInterface<T> column,
        GenerateSqlPartFunctionalInterface<T, K> closure) {
        return havingNotIn(lambda2ColumnName(column), closure);
    }

    /**
     * 列值在两值之间
     * @param column 列名表达式
     * @param min 值1
     * @param max 值2
     * @return 查询构造器
     */
    default Builder<T, K> havingBetween(ColumnFunctionalInterface<T> column, Object min, Object max) {
        return havingBetween(lambda2ColumnName(column), min, max);
    }

    /**
     * 列值不在两值之间
     * @param column 列名表达式
     * @param min 值1
     * @param max 值2
     * @return 查询构造器
     */
    default Builder<T, K> havingNotBetween(ColumnFunctionalInterface<T> column, Object min, Object max) {
        return havingNotBetween(lambda2ColumnName(column), min, max);
    }

    /**
     * 列值为null
     * @param column 列名表达式
     * @return 查询构造器
     */
    default Builder<T, K> havingNull(ColumnFunctionalInterface<T> column) {
        return havingNull(lambda2ColumnName(column));
    }

    /**
     * 列值不为null
     * @param column 列名表达式
     * @return 查询构造器
     */
    default Builder<T, K> havingNotNull(ColumnFunctionalInterface<T> column) {
        return havingNotNull(lambda2ColumnName(column));
    }

    /**
     * 比较字段与字段
     * @param column1 列1 表达式
     * @param symbol 比较关系
     * @param column2 列2 表达式
     * @return 查询构造器
     */
    default Builder<T, K> havingColumn(ColumnFunctionalInterface<T> column1, String symbol,
        ColumnFunctionalInterface<T> column2) {
        return havingColumn(lambda2ColumnName(column1), symbol, lambda2ColumnName(column2));
    }

    /**
     * 字段与字段相等
     * @param column1 列1 表达式
     * @param column2 列2 表达式
     * @return 查询构造器
     */
    default Builder<T, K> havingColumn(ColumnFunctionalInterface<T> column1, ColumnFunctionalInterface<T> column2) {
        return havingColumn(lambda2ColumnName(column1), lambda2ColumnName(column2));
    }

}
