package gaarason.database.contract.builder;

import gaarason.database.contract.eloquent.Builder;
import gaarason.database.contract.function.BuilderWrapper;
import gaarason.database.contract.function.ColumnFunctionalInterface;
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
     * @param <F> 属性类型
     * @return 查询构造器
     */
    default <F> Builder<T, K> having(ColumnFunctionalInterface<T, F> column, String symbol, Object value) {
        return having(lambda2ColumnName(column), symbol, value);
    }

    /**
     * 列包含选项值
     * @param column 列名表达式(位存储)
     * @param value 选项值(eg: 0,1,2,3)
     * @return 查询构造器
     */
    default <F> Builder<T, K> havingBit(ColumnFunctionalInterface<T, F> column, Object value) {
        return havingBit(lambda2ColumnName(column), value);
    }

    /**
     * 列不包含选项值
     * @param column 列名表达式(位存储)
     * @param value 选项值(eg: 0,1,2,3)
     * @return 查询构造器
     */
    default <F> Builder<T, K> havingBitNot(ColumnFunctionalInterface<T, F> column, Object value) {
        return havingBitNot(lambda2ColumnName(column), value);
    }

    /**
     * 列包含选项值其一
     * @param column 列名表达式(位存储)
     * @param values 选项值(eg: 0,1,2,3)
     * @return 查询构造器
     */
    default <F, W> Builder<T, K> havingBitIn(ColumnFunctionalInterface<T, F> column, Collection<W> values) {
        return havingBitIn(lambda2ColumnName(column), values);
    }

    /**
     * 列不包含选项值其一
     * @param column 列名表达式(位存储)
     * @param values 选项值(eg: 0,1,2,3)
     * @return 查询构造器
     */
    default <F> Builder<T, K> havingBitNotIn(ColumnFunctionalInterface<T, F> column, Collection<?> values) {
        return havingBitNotIn(lambda2ColumnName(column), values);
    }

    /**
     * 列完全包含所有选项值
     * @param column 列名表达式(位存储)
     * @param values 选项值(eg: 0,1,2,3)
     * @return 查询构造器
     */
    default <F> Builder<T, K> havingBitStrictIn(ColumnFunctionalInterface<T, F> column, Collection<?> values) {
        return havingBitStrictIn(lambda2ColumnName(column), values);
    }

    /**
     * 列完全不包含所有选项值
     * @param column 列名表达式(位存储)
     * @param values 选项值(eg: 0,1,2,3)
     * @return 查询构造器
     */
    default <F> Builder<T, K> havingBitStrictNotIn(ColumnFunctionalInterface<T, F> column, Collection<?> values) {
        return havingBitStrictNotIn(lambda2ColumnName(column), values);
    }


    /**
     * 比较列与值(忽略值为null的情况)
     * @param column 列名表达式
     * @param symbol 比较关系
     * @param value 值
     * @param <F> 属性类型
     * @return 查询构造器
     */
    default <F> Builder<T, K> havingIgnoreNull(ColumnFunctionalInterface<T, F> column, String symbol, @Nullable Object value) {
        return havingIgnoreNull(lambda2ColumnName(column), symbol, value);
    }

    /**
     * 比较列与值相等
     * @param column 列名表达式
     * @param value 值
     * @param <F> 属性类型
     * @return 查询构造器
     */
    default <F> Builder<T, K> having(ColumnFunctionalInterface<T, F> column, @Nullable Object value) {
        return having(lambda2ColumnName(column), value);
    }

    /**
     * 比较列与值相等(忽略值为null的情况)
     * @param column 列名表达式
     * @param value 值
     * @param <F> 属性类型
     * @return 查询构造器
     */
    default <F> Builder<T, K> havingIgnoreNull(ColumnFunctionalInterface<T, F> column, @Nullable Object value) {
        return havingIgnoreNull(lambda2ColumnName(column), value);
    }

    /**
     * 在多个列中, 查找值
     * 当 value 以 %开头或者结尾时, 使用like查询
     * 当 value 为 null 时, 使用 is null 查询
     * 其他情况下, 使用 = 查询
     * @param value 值
     * @param columns 多个列名表达式
     * @param <F> 属性类型
     * @return 查询构造器
     */
    @SuppressWarnings("unchecked")
    default <F> Builder<T, K> havingKeywords(@Nullable Object value, ColumnFunctionalInterface<T, F>... columns) {
        return havingKeywords(value, lambda2ColumnName(Arrays.asList(columns)));
    }

    /**
     * 在多个列中, 查找值
     * 当 value 以 %开头或者结尾时, 使用like查询
     * 当 value 为 null 时, 忽略
     * 其他情况下, 使用 = 查询
     * @param value 值
     * @param columns 多个列名表达式
     * @param <F> 属性类型
     * @return 查询构造器
     */
    @SuppressWarnings("unchecked")
    default <F> Builder<T, K> havingKeywordsIgnoreNull(@Nullable Object value, ColumnFunctionalInterface<T, F>... columns) {
        return havingKeywordsIgnoreNull(value, lambda2ColumnName(Arrays.asList(columns)));
    }

    /**
     * "列like值" 的查询条件
     * 其中值如果没有在开头或结尾自行包含 % 符号，则在开头以及结尾拼接 % 符号
     * 忽略值为null的情况
     * 忽略值为 % 、%%的情况
     * @param column 列名表达式
     * @param value 值
     * @param <F> 属性类型
     * @return 查询构造器
     */
    default <F> Builder<T, K> havingLike(ColumnFunctionalInterface<T, F> column, @Nullable Object value) {
        return havingLike(lambda2ColumnName(column), value);
    }

    /**
     * "列 not like值" 的查询条件
     * 其中值如果没有在开头或结尾自行包含 % 符号，则在开头以及结尾拼接 % 符号
     * 忽略值为null的情况
     * 忽略值为 % 、%%的情况
     * @param column 列名表达式
     * @param value 值
     * @param <F> 属性类型
     * @return 查询构造器
     */
    default <F> Builder<T, K> havingNotLike(ColumnFunctionalInterface<T, F> column, @Nullable Object value) {
        return havingNotLike(lambda2ColumnName(column), value);
    }

    /**
     * 选择可能的条件类型
     * 当 value 以 %开头或者结尾时, 使用like查询
     * 当 value 为 null 时, 使用 is null 查询
     * 其他情况下, 使用 = 查询
     * @param column 列名表达式
     * @param value 值
     * @param <F> 属性类型
     * @return 查询构造器
     */
    default <F> Builder<T, K> havingMayLike(ColumnFunctionalInterface<T, F> column, @Nullable Object value) {
        return havingMayLike(lambda2ColumnName(column), value);
    }

    /**
     * 选择可能的条件类型
     * 当 value 以 %开头或者结尾时, 使用like查询
     * 当 value 为 null 时, 使用 is null 查询
     * 其他情况下, 使用 = 查询
     * @param column 列名表达式
     * @param value 值
     * @param <F> 属性类型
     * @return 查询构造器
     */
    default <F> Builder<T, K> havingMayNotLike(ColumnFunctionalInterface<T, F> column, @Nullable Object value) {
        return havingMayNotLike(lambda2ColumnName(column), value);
    }

    /**
     * 选择可能的条件类型
     * 当 value 以 %开头或者结尾时, 使用like查询
     * 当 value 为 null 时, 忽略
     * 其他情况下, 使用 = 查询
     * @param column 列名表达式
     * @param value 值
     * @param <F> 属性类型
     * @return 查询构造器
     */
    default <F> Builder<T, K> havingMayLikeIgnoreNull(ColumnFunctionalInterface<T, F> column, @Nullable Object value) {
        return havingMayLikeIgnoreNull(lambda2ColumnName(column), value);
    }

    /**
     * 选择可能的条件类型
     * 当 value 以 %开头或者结尾时, 使用not like查询
     * 当 value 为 null 时, 忽略
     * 其他情况下, 使用 != 查询
     * @param column 列名
     * @param value 值
     * @param <F> 属性类型
     * @return 查询构造器
     */
    default <F> Builder<T, K> havingMayNotLikeIgnoreNull(ColumnFunctionalInterface<T, F> column, @Nullable Object value) {
        return havingMayNotLikeIgnoreNull(lambda2ColumnName(column), value);
    }

    /**
     * 条件子查询
     * @param column 列名表达式
     * @param symbol 关系符号
     * @param completeSql 完整sql
     * @param <F> 属性类型
     * @return 查询构造器
     */
    default <F> Builder<T, K> havingSubQuery(ColumnFunctionalInterface<T, F> column, String symbol, String completeSql) {
        return havingSubQuery(lambda2ColumnName(column), symbol, completeSql);
    }

    /**
     * 条件子查询
     * @param column 列名表达式
     * @param symbol 关系符号
     * @param closure 闭包
     * @param <F> 属性类型
     * @return 查询构造器
     */
    default <F> Builder<T, K> havingSubQuery(ColumnFunctionalInterface<T, F> column, String symbol,
        BuilderWrapper<T, K> closure) {
        return havingSubQuery(lambda2ColumnName(column), symbol, closure);
    }

    /**
     * 列值在范围内
     * @param column 列名表达式
     * @param valueList 值所在的list
     * @param <F> 属性类型
     * @return 查询构造器
     */
    default <F> Builder<T, K> havingIn(ColumnFunctionalInterface<T, F> column, Collection<?> valueList) {
        return havingIn(lambda2ColumnName(column), valueList);
    }

    /**
     * 列值在范围内(忽略值为空的情况)
     * @param column 列名表达式
     * @param valueList 值所在的list
     * @param <F> 属性类型
     * @return 查询构造器
     */
    default <F> Builder<T, K> havingInIgnoreEmpty(ColumnFunctionalInterface<T, F> column, @Nullable Collection<?> valueList) {
        return havingInIgnoreEmpty(lambda2ColumnName(column), valueList);
    }

    /**
     * 列值在范围内
     * @param column 列名表达式
     * @param valueArray 值所在的数组
     * @param <F> 属性类型
     * @return 查询构造器
     */
    default <F> Builder<T, K> havingIn(ColumnFunctionalInterface<T, F> column, Object... valueArray) {
        return havingIn(lambda2ColumnName(column), valueArray);
    }

    /**
     * 列值在范围内(忽略值为空的情况)
     * @param column 列名表达式
     * @param valueArray 值所在的数组
     * @param <F> 属性类型
     * @return 查询构造器
     */
    default <F> Builder<T, K> havingInIgnoreEmpty(ColumnFunctionalInterface<T, F> column, @Nullable Object... valueArray) {
        return havingInIgnoreEmpty(lambda2ColumnName(column), valueArray);
    }

    /**
     * 列值在范围内(子查询)
     * @param column 列名表达式
     * @param sql 完整sql eg:select id from student having age>10
     * @param <F> 属性类型
     * @return 查询构造器
     */
    default <F> Builder<T, K> havingInRaw(ColumnFunctionalInterface<T, F> column, String sql) {
        return havingInRaw(lambda2ColumnName(column), sql);
    }

    /**
     * 列值在范围内(子查询)
     * @param column 列名表达式
     * @param closure 闭包
     * @param <F> 属性类型
     * @return 查询构造器
     */
    default <F> Builder<T, K> havingIn(ColumnFunctionalInterface<T, F> column,
        BuilderWrapper<T, K> closure) {
        return havingIn(lambda2ColumnName(column), closure);
    }

    /**
     * 列值不在范围内
     * @param column 列名表达式
     * @param valueList 值所在的list
     * @param <F> 属性类型
     * @return 查询构造器
     */
    default <F> Builder<T, K> havingNotIn(ColumnFunctionalInterface<T, F> column, Collection<?> valueList) {
        return havingNotIn(lambda2ColumnName(column), valueList);
    }

    /**
     * 列值在范围内(忽略值为空的情况)
     * @param column 列名表达式
     * @param valueList 值所在的list
     * @param <F> 属性类型
     * @return 查询构造器
     */
    default <F> Builder<T, K> havingNotInIgnoreEmpty(ColumnFunctionalInterface<T, F> column,
        @Nullable Collection<?> valueList) {
        return havingNotInIgnoreEmpty(lambda2ColumnName(column), valueList);
    }

    /**
     * 列值在范围内
     * @param column 列名表达式
     * @param valueArray 值所在的数组
     * @param <F> 属性类型
     * @return 查询构造器
     */
    default <F> Builder<T, K> havingNotIn(ColumnFunctionalInterface<T, F> column, Object... valueArray) {
        return havingNotIn(lambda2ColumnName(column), valueArray);
    }

    /**
     * 列值在范围内(忽略值为空的情况)
     * @param column 列名表达式
     * @param valueArray 值所在的数组
     * @param <F> 属性类型
     * @return 查询构造器
     */
    default <F> Builder<T, K> havingNotInIgnoreEmpty(ColumnFunctionalInterface<T, F> column, @Nullable Object... valueArray) {
        return havingNotInIgnoreEmpty(lambda2ColumnName(column), valueArray);
    }

    /**
     * 列值不在范围内(子查询)
     * @param column 列名表达式
     * @param sql 完整sql eg:select id from student having age>10
     * @param <F> 属性类型
     * @return 查询构造器
     */
    default <F> Builder<T, K> havingNotInRaw(ColumnFunctionalInterface<T, F> column, String sql) {
        return havingNotInRaw(lambda2ColumnName(column), sql);
    }

    /**
     * 列值不在范围内(子查询)
     * @param column 列名表达式
     * @param closure 闭包
     * @param <F> 属性类型
     * @return 查询构造器
     */
    default <F> Builder<T, K> havingNotIn(ColumnFunctionalInterface<T, F> column,
        BuilderWrapper<T, K> closure) {
        return havingNotIn(lambda2ColumnName(column), closure);
    }

    /**
     * 列值在两值之间
     * @param column 列名表达式
     * @param min 值1
     * @param max 值2
     * @param <F> 属性类型
     * @return 查询构造器
     */
    default <F> Builder<T, K> havingBetween(ColumnFunctionalInterface<T, F> column, Object min, Object max) {
        return havingBetween(lambda2ColumnName(column), min, max);
    }

    /**
     * 列值不在两值之间
     * @param column 列名表达式
     * @param min 值1
     * @param max 值2
     * @param <F> 属性类型
     * @return 查询构造器
     */
    default <F> Builder<T, K> havingNotBetween(ColumnFunctionalInterface<T, F> column, Object min, Object max) {
        return havingNotBetween(lambda2ColumnName(column), min, max);
    }

    /**
     * 列值为null
     * @param column 列名表达式
     * @param <F> 属性类型
     * @return 查询构造器
     */
    default <F> Builder<T, K> havingNull(ColumnFunctionalInterface<T, F> column) {
        return havingNull(lambda2ColumnName(column));
    }

    /**
     * 列值不为null
     * @param column 列名表达式
     * @param <F> 属性类型
     * @return 查询构造器
     */
    default <F> Builder<T, K> havingNotNull(ColumnFunctionalInterface<T, F> column) {
        return havingNotNull(lambda2ColumnName(column));
    }

    /**
     * 比较字段与字段
     * @param column1 列1 表达式
     * @param symbol 比较关系
     * @param column2 列2 表达式
     * @param <F> 属性类型
     * @return 查询构造器
     */
    default <F> Builder<T, K> havingColumn(ColumnFunctionalInterface<T, F> column1, String symbol,
        ColumnFunctionalInterface<T, F> column2) {
        return havingColumn(lambda2ColumnName(column1), symbol, lambda2ColumnName(column2));
    }

    /**
     * 字段与字段相等
     * @param column1 列1 表达式
     * @param column2 列2 表达式
     * @param <F> 属性类型
     * @return 查询构造器
     */
    default <F> Builder<T, K> havingColumn(ColumnFunctionalInterface<T, F> column1, ColumnFunctionalInterface<T, F> column2) {
        return havingColumn(lambda2ColumnName(column1), lambda2ColumnName(column2));
    }

}
