package gaarason.database.contract.builder;

import gaarason.database.contract.eloquent.Builder;
import gaarason.database.contract.function.ColumnFunctionalInterface;
import gaarason.database.contract.function.GenerateSqlPartFunctionalInterface;
import gaarason.database.lang.Nullable;

import java.util.Arrays;
import java.util.Collection;

/**
 * 条件
 * @param <T> 实体类型
 * @param <K> 主键类型
 * @author xt
 */
public interface WhereLambda<T, K> extends Where<T, K>, Support<T, K> {

    /**
     * 比较列与值
     * @param column 列名表达式
     * @param symbol 比较关系
     * @param value 值
     * @param <F> 属性类型
     * @return 查询构造器
     */
    default <F> Builder<T, K> where(ColumnFunctionalInterface<T, F> column, String symbol, Object value) {
        return where(lambda2ColumnName(column), symbol, value);
    }

    /**
     * 比较列与值(忽略值为null的情况)
     * @param column 列名表达式
     * @param symbol 比较关系
     * @param value 值
     * @param <F> 属性类型
     * @return 查询构造器
     */
    default <F> Builder<T, K> whereIgnoreNull(ColumnFunctionalInterface<T, F> column, String symbol, @Nullable Object value) {
        return whereIgnoreNull(lambda2ColumnName(column), symbol, value);
    }

    /**
     * 比较列与值相等
     * @param column 列名表达式
     * @param value 值
     * @param <F> 属性类型
     * @return 查询构造器
     */
    default <F> Builder<T, K> where(ColumnFunctionalInterface<T, F> column, @Nullable Object value) {
        return where(lambda2ColumnName(column), value);
    }

//    default <F> Builder<T, K> where(ColumnFunctionalInterface.Collection<T, F> column, @Nullable Object value) {
//        return where(lambda2ColumnName(column), value);
//    }

    /**
     * 比较列与值相等(忽略值为null的情况)
     * @param column 列名表达式
     * @param value 值
     * @param <F> 属性类型
     * @return 查询构造器
     */
    default <F> Builder<T, K> whereIgnoreNull(ColumnFunctionalInterface<T, F> column, @Nullable Object value) {
        return whereIgnoreNull(lambda2ColumnName(column), value);
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
    default <F> Builder<T, K> whereKeywords(@Nullable Object value, ColumnFunctionalInterface<T, F>... columns) {
        return whereKeywordsIgnoreNull(value, lambda2ColumnName(Arrays.asList(columns)));
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
    default <F> Builder<T, K> whereLikeIgnoreNull(ColumnFunctionalInterface<T, F> column, @Nullable Object value) {
        return whereLikeIgnoreNull(lambda2ColumnName(column), value);
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
    default <F> Builder<T, K> whereNotLikeIgnoreNull(ColumnFunctionalInterface<T, F> column, @Nullable Object value) {
        return whereNotLikeIgnoreNull(lambda2ColumnName(column), value);
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
    default <F> Builder<T, K> whereMayLike(ColumnFunctionalInterface<T, F> column, @Nullable Object value) {
        return whereMayLike(lambda2ColumnName(column), value);
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
    default <F> Builder<T, K> whereMayNotLike(ColumnFunctionalInterface<T, F> column, @Nullable Object value) {
        return whereMayNotLike(lambda2ColumnName(column), value);
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
    default <F> Builder<T, K> whereMayLikeIgnoreNull(ColumnFunctionalInterface<T, F> column, @Nullable Object value) {
        return whereMayLikeIgnoreNull(lambda2ColumnName(column), value);
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
    default <F> Builder<T, K> whereMayNotLikeIgnoreNull(ColumnFunctionalInterface<T, F> column, @Nullable Object value) {
        return whereMayNotLikeIgnoreNull(lambda2ColumnName(column), value);
    }

    /**
     * 条件子查询
     * @param column 列名表达式
     * @param symbol 关系符号
     * @param completeSql 完整sql
     * @param <F> 属性类型
     * @return 查询构造器
     */
    default <F> Builder<T, K> whereSubQuery(ColumnFunctionalInterface<T, F> column, String symbol, String completeSql) {
        return whereSubQuery(lambda2ColumnName(column), symbol, completeSql);
    }

    /**
     * 条件子查询
     * @param column 列名表达式
     * @param symbol 关系符号
     * @param closure 闭包
     * @param <F> 属性类型
     * @return 查询构造器
     */
    default <F> Builder<T, K> whereSubQuery(ColumnFunctionalInterface<T, F> column, String symbol,
        GenerateSqlPartFunctionalInterface<T, K> closure) {
        return whereSubQuery(lambda2ColumnName(column), symbol, closure);
    }

    /**
     * 列值在范围内
     * @param column 列名表达式
     * @param valueList 值所在的list
     * @param <F> 属性类型
     * @return 查询构造器
     */
    default <F> Builder<T, K> whereIn(ColumnFunctionalInterface<T, F> column, Collection<?> valueList) {
        return whereIn(lambda2ColumnName(column), valueList);
    }

    /**
     * 列值在范围内(忽略值为空的情况)
     * @param column 列名表达式
     * @param valueList 值所在的list
     * @param <F> 属性类型
     * @return 查询构造器
     */
    default <F> Builder<T, K> whereInIgnoreEmpty(ColumnFunctionalInterface<T, F> column, @Nullable Collection<?> valueList) {
        return whereInIgnoreEmpty(lambda2ColumnName(column), valueList);
    }

    /**
     * 列值在范围内
     * @param column 列名表达式
     * @param valueArray 值所在的数组
     * @param <F> 属性类型
     * @return 查询构造器
     */
    default <F> Builder<T, K> whereIn(ColumnFunctionalInterface<T, F> column, Object... valueArray) {
        return whereIn(lambda2ColumnName(column), valueArray);
    }

    /**
     * 列值在范围内(忽略值为空的情况)
     * @param column 列名表达式
     * @param valueArray 值所在的数组
     * @param <F> 属性类型
     * @return 查询构造器
     */
    default <F> Builder<T, K> whereInIgnoreEmpty(ColumnFunctionalInterface<T, F> column, @Nullable Object... valueArray) {
        return whereInIgnoreEmpty(lambda2ColumnName(column), valueArray);
    }

    /**
     * 列值在范围内(子查询)
     * @param column 列名表达式
     * @param sql 完整sql eg:select id from student where age>10
     * @param <F> 属性类型
     * @return 查询构造器
     */
    default <F> Builder<T, K> whereInRaw(ColumnFunctionalInterface<T, F> column, String sql) {
        return whereInRaw(lambda2ColumnName(column), sql);
    }

    /**
     * 列值在范围内(子查询)
     * @param column 列名表达式
     * @param closure 闭包
     * @param <F> 属性类型
     * @return 查询构造器
     */
    default <F> Builder<T, K> whereIn(ColumnFunctionalInterface<T, F> column,
        GenerateSqlPartFunctionalInterface<T, K> closure) {
        return whereIn(lambda2ColumnName(column), closure);
    }

    /**
     * 列值不在范围内
     * @param column 列名表达式
     * @param valueList 值所在的list
     * @param <F> 属性类型
     * @return 查询构造器
     */
    default <F> Builder<T, K> whereNotIn(ColumnFunctionalInterface<T, F> column, Collection<?> valueList) {
        return whereNotIn(lambda2ColumnName(column), valueList);
    }

    /**
     * 列值在范围内(忽略值为空的情况)
     * @param column 列名表达式
     * @param valueList 值所在的list
     * @param <F> 属性类型
     * @return 查询构造器
     */
    default <F> Builder<T, K> whereNotInIgnoreEmpty(ColumnFunctionalInterface<T, F> column,
        @Nullable Collection<?> valueList) {
        return whereNotInIgnoreEmpty(lambda2ColumnName(column), valueList);
    }

    /**
     * 列值在范围内
     * @param column 列名表达式
     * @param valueArray 值所在的数组
     * @param <F> 属性类型
     * @return 查询构造器
     */
    default <F> Builder<T, K> whereNotIn(ColumnFunctionalInterface<T, F> column, Object... valueArray) {
        return whereNotIn(lambda2ColumnName(column), valueArray);
    }

    /**
     * 列值在范围内(忽略值为空的情况)
     * @param column 列名表达式
     * @param valueArray 值所在的数组
     * @param <F> 属性类型
     * @return 查询构造器
     */
    default <F> Builder<T, K> whereNotInIgnoreEmpty(ColumnFunctionalInterface<T, F> column, @Nullable Object... valueArray) {
        return whereNotInIgnoreEmpty(lambda2ColumnName(column), valueArray);
    }

    /**
     * 列值不在范围内(子查询)
     * @param column 列名表达式
     * @param sql 完整sql eg:select id from student where age>10
     * @param <F> 属性类型
     * @return 查询构造器
     */
    default <F> Builder<T, K> whereNotInRaw(ColumnFunctionalInterface<T, F> column, String sql) {
        return whereNotInRaw(lambda2ColumnName(column), sql);
    }

    /**
     * 列值不在范围内(子查询)
     * @param column 列名表达式
     * @param closure 闭包
     * @param <F> 属性类型
     * @return 查询构造器
     */
    default <F> Builder<T, K> whereNotIn(ColumnFunctionalInterface<T, F> column,
        GenerateSqlPartFunctionalInterface<T, K> closure) {
        return whereNotIn(lambda2ColumnName(column), closure);
    }

    /**
     * 列值在两值之间
     * @param column 列名表达式
     * @param min 值1
     * @param max 值2
     * @param <F> 属性类型
     * @return 查询构造器
     */
    default <F> Builder<T, K> whereBetween(ColumnFunctionalInterface<T, F> column, Object min, Object max) {
        return whereBetween(lambda2ColumnName(column), min, max);
    }

    /**
     * 列值不在两值之间
     * @param column 列名表达式
     * @param min 值1
     * @param max 值2
     * @param <F> 属性类型
     * @return 查询构造器
     */
    default <F> Builder<T, K> whereNotBetween(ColumnFunctionalInterface<T, F> column, Object min, Object max) {
        return whereNotBetween(lambda2ColumnName(column), min, max);
    }

    /**
     * 列值为null
     * @param column 列名表达式
     * @param <F> 属性类型
     * @return 查询构造器
     */
    default <F> Builder<T, K> whereNull(ColumnFunctionalInterface<T, F> column) {
        return whereNull(lambda2ColumnName(column));
    }

    /**
     * 列值不为null
     * @param column 列名表达式
     * @param <F> 属性类型
     * @return 查询构造器
     */
    default <F> Builder<T, K> whereNotNull(ColumnFunctionalInterface<T, F> column) {
        return whereNotNull(lambda2ColumnName(column));
    }

    /**
     * 比较字段与字段
     * @param column1 列1 表达式
     * @param symbol 比较关系
     * @param column2 列2 表达式
     * @param <F> 属性类型
     * @return 查询构造器
     */
    default <F> Builder<T, K> whereColumn(ColumnFunctionalInterface<T, F> column1, String symbol,
        ColumnFunctionalInterface<T, F> column2) {
        return whereColumn(lambda2ColumnName(column1), symbol, lambda2ColumnName(column2));
    }

    /**
     * 字段与字段相等
     * @param column1 列1 表达式
     * @param column2 列2 表达式
     * @param <F> 属性类型
     * @return 查询构造器
     */
    default <F> Builder<T, K> whereColumn(ColumnFunctionalInterface<T, F> column1, ColumnFunctionalInterface<T, F> column2) {
        return whereColumn(lambda2ColumnName(column1), lambda2ColumnName(column2));
    }

}
