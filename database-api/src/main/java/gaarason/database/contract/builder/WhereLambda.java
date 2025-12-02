package gaarason.database.contract.builder;

import gaarason.database.contract.eloquent.Builder;
import gaarason.database.contract.function.BuilderAnyWrapper;
import gaarason.database.contract.function.BuilderEntityWrapper;
import gaarason.database.contract.function.BuilderWrapper;
import gaarason.database.contract.function.ColumnFunctionalInterface;
import gaarason.database.lang.Nullable;

import java.util.Arrays;
import java.util.Collection;

/**
 * 条件
 * @param <T> 实体类型
 * @param <K> 主键类型
 * @author xt
 */
public interface WhereLambda<B extends Builder<B, T, K>, T, K> extends Where<B, T, K>, Support<B, T, K> {

    /**
     * 比较列与值
     * @param column 列名表达式
     * @param symbol 比较关系
     * @param value 值
     * @param <F> 属性类型
     * @return 查询构造器
     */
    default <F> B where(ColumnFunctionalInterface<T, F> column, String symbol, Object value) {
        return where(lambda2ColumnName(column), symbol, value);
    }
    /**
     * 列包含选项值
     * @param column 列名表达式(位存储)
     * @param value 选项值(eg: 0,1,2,3)
     * @return 查询构造器
     */
    default <F> B whereBit(ColumnFunctionalInterface<T, F> column, Object value) {
        return whereBit(lambda2ColumnName(column), value);
    }

    /**
     * 列不包含选项值
     * @param column 列名表达式(位存储)
     * @param value 选项值(eg: 0,1,2,3)
     * @return 查询构造器
     */
    default <F> B whereBitNot(ColumnFunctionalInterface<T, F> column, Object value) {
        return whereBitNot(lambda2ColumnName(column), value);
    }

    /**
     * 列包含选项值其一
     * @param column 列名表达式(位存储)
     * @param values 选项值(eg: 0,1,2,3)
     * @return 查询构造器
     */
    default <F, W> B whereBitIn(ColumnFunctionalInterface<T, F> column, Collection<W> values) {
        return whereBitIn(lambda2ColumnName(column), values);
    }

    /**
     * 列不包含选项值其一
     * @param column 列名表达式(位存储)
     * @param values 选项值(eg: 0,1,2,3)
     * @return 查询构造器
     */
    default <F> B whereBitNotIn(ColumnFunctionalInterface<T, F> column, Collection<?> values) {
        return whereBitNotIn(lambda2ColumnName(column), values);
    }

    /**
     * 列完全包含所有选项值
     * @param column 列名表达式(位存储)
     * @param values 选项值(eg: 0,1,2,3)
     * @return 查询构造器
     */
    default <F> B whereBitStrictIn(ColumnFunctionalInterface<T, F> column, Collection<?> values) {
        return whereBitStrictIn(lambda2ColumnName(column), values);
    }

    /**
     * 列完全不包含所有选项值
     * @param column 列名表达式(位存储)
     * @param values 选项值(eg: 0,1,2,3)
     * @return 查询构造器
     */
    default <F> B whereBitStrictNotIn(ColumnFunctionalInterface<T, F> column, Collection<?> values) {
        return whereBitStrictNotIn(lambda2ColumnName(column), values);
    }

    /**
     * 比较列与值(忽略值为null的情况)
     * @param column 列名表达式
     * @param symbol 比较关系
     * @param value 值
     * @param <F> 属性类型
     * @return 查询构造器
     */
    default <F> B whereIgnoreNull(ColumnFunctionalInterface<T, F> column, String symbol,
        @Nullable Object value) {
        return whereIgnoreNull(lambda2ColumnName(column), symbol, value);
    }

    /**
     * 比较列与值相等
     * @param column 列名表达式
     * @param value 值
     * @param <F> 属性类型
     * @return 查询构造器
     */
    default <F> B where(ColumnFunctionalInterface<T, F> column, @Nullable Object value) {
        return where(lambda2ColumnName(column), value);
    }

    /**
     * 比较列与值相等(忽略值为null的情况)
     * @param column 列名表达式
     * @param value 值
     * @param <F> 属性类型
     * @return 查询构造器
     */
    default <F> B whereIgnoreNull(ColumnFunctionalInterface<T, F> column, @Nullable Object value) {
        return whereIgnoreNull(lambda2ColumnName(column), value);
    }

    /**
     * 在多个列中, 查找值, 任一满足
     * 忽略 value 为 null 、"" 、% 、%% 的情况
     * 其中值如果没有在开头或结尾自行包含 % 符号，则在开头以及结尾拼接 % 符号
     * sql eg : where ( column1 like %value% or column2 like %value% )
     * @param value 值
     * @param columns 列名
     * @return 查询构造器
     */
    @SuppressWarnings("unchecked")
    default <F> B whereAnyLike(@Nullable Object value, ColumnFunctionalInterface<T, F>... columns) {
        return whereAnyLike(value, lambda2ColumnName(Arrays.asList(columns)));
    }

    /**
     * 在多个列中, 查找值, 全部满足
     * 忽略 value 为 null 、"" 、% 、%% 的情况
     * 其中值如果没有在开头或结尾自行包含 % 符号，则在开头以及结尾拼接 % 符号
     * sql eg : where ( column1 like %value% and column2 like %value% )
     * @param value 值
     * @param columns 列名集合
     * @return 查询构造器
     */
    @SuppressWarnings("unchecked")
    default <F> B whereAllLike(@Nullable Object value, ColumnFunctionalInterface<T, F>... columns) {
        return whereAllLike(value, lambda2ColumnName(Arrays.asList(columns)));
    }

    /**
     * "列like值" 的查询条件
     * 忽略 value 为 null 、"" 、% 、%% 的情况
     * 其中值如果没有在开头或结尾自行包含 % 符号，则在开头以及结尾拼接 % 符号
     * @param column 列名表达式
     * @param value 值
     * @param <F> 属性类型
     * @return 查询构造器
     */
    default <F> B whereLike(ColumnFunctionalInterface<T, F> column, @Nullable Object value) {
        return whereLike(lambda2ColumnName(column), value);
    }

    /**
     * "列 not like值" 的查询条件
     * 忽略 value 为 null 、"" 、% 、%% 的情况
     * 其中值如果没有在开头或结尾自行包含 % 符号，则在开头以及结尾拼接 % 符号
     * @param column 列名表达式
     * @param value 值
     * @param <F> 属性类型
     * @return 查询构造器
     */
    default <F> B whereNotLike(ColumnFunctionalInterface<T, F> column, @Nullable Object value) {
        return whereNotLike(lambda2ColumnName(column), value);
    }

    /**
     * 选择可能的条件类型
     * 忽略 value 为 % 、%% 的情况
     * 当 value 以 %开头或者结尾时, 使用like查询
     * 当 value 为 null 时, 使用 is null 查询
     * 其他情况下, 使用 = 查询
     * @param column 列名表达式
     * @param value 值
     * @param <F> 属性类型
     * @return 查询构造器
     */
    default <F> B whereMayLike(ColumnFunctionalInterface<T, F> column, @Nullable Object value) {
        return whereMayLike(lambda2ColumnName(column), value);
    }

    /**
     * 选择可能的条件类型
     * 忽略 value 为 % 、%% 的情况
     * 当 value 以 %开头或者结尾时, 使用like查询
     * 当 value 为 null 时, 使用 is null 查询
     * 其他情况下, 使用 = 查询
     * @param column 列名表达式
     * @param value 值
     * @param <F> 属性类型
     * @return 查询构造器
     */
    default <F> B whereMayNotLike(ColumnFunctionalInterface<T, F> column, @Nullable Object value) {
        return whereMayNotLike(lambda2ColumnName(column), value);
    }

    /**
     * 选择可能的条件类型
     * 忽略 value 为 null 、% 、%% 的情况
     * 当 value 以 %开头或者结尾时, 使用like查询
     * 当 value 为 null 时, 忽略
     * 其他情况下, 使用 = 查询
     * @param column 列名表达式
     * @param value 值
     * @param <F> 属性类型
     * @return 查询构造器
     */
    default <F> B whereMayLikeIgnoreNull(ColumnFunctionalInterface<T, F> column, @Nullable Object value) {
        return whereMayLikeIgnoreNull(lambda2ColumnName(column), value);
    }

    /**
     * 选择可能的条件类型
     * 忽略 value 为 null 、% 、%% 的情况
     * 当 value 以 %开头或者结尾时, 使用not like查询
     * 当 value 为 null 时, 忽略
     * 其他情况下, 使用 != 查询
     * @param column 列名
     * @param value 值
     * @param <F> 属性类型
     * @return 查询构造器
     */
    default <F> B whereMayNotLikeIgnoreNull(ColumnFunctionalInterface<T, F> column,
        @Nullable Object value) {
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
    default <F> B whereSubQuery(ColumnFunctionalInterface<T, F> column, String symbol, String completeSql) {
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
    default <F> B whereSubQuery(ColumnFunctionalInterface<T, F> column, String symbol,
        BuilderWrapper<B, T, K> closure) {
        return whereSubQuery(lambda2ColumnName(column), symbol, closure);
    }

    /**
     * 列值在范围内
     * @param column 列名表达式
     * @param valueList 值所在的list
     * @param <F> 属性类型
     * @return 查询构造器
     */
    default <F> B whereIn(ColumnFunctionalInterface<T, F> column, Collection<?> valueList) {
        return whereIn(lambda2ColumnName(column), valueList);
    }

    /**
     * 列值在范围内(忽略值为空的情况)
     * @param column 列名表达式
     * @param valueList 值所在的list
     * @param <F> 属性类型
     * @return 查询构造器
     */
    default <F> B whereInIgnoreEmpty(ColumnFunctionalInterface<T, F> column,
        @Nullable Collection<?> valueList) {
        return whereInIgnoreEmpty(lambda2ColumnName(column), valueList);
    }

    /**
     * 列值在范围内
     * @param column 列名表达式
     * @param valueArray 值所在的数组
     * @param <F> 属性类型
     * @return 查询构造器
     */
    default <F> B whereIn(ColumnFunctionalInterface<T, F> column, Object... valueArray) {
        return whereIn(lambda2ColumnName(column), valueArray);
    }

    /**
     * 列值在范围内(忽略值为空的情况)
     * @param column 列名表达式
     * @param valueArray 值所在的数组
     * @param <F> 属性类型
     * @return 查询构造器
     */
    default <F> B whereInIgnoreEmpty(ColumnFunctionalInterface<T, F> column,
        @Nullable Object... valueArray) {
        return whereInIgnoreEmpty(lambda2ColumnName(column), valueArray);
    }

    /**
     * 列值在范围内(子查询)
     * @param column 列名表达式
     * @param sql 完整sql eg:select id from student where age>10
     * @param <F> 属性类型
     * @return 查询构造器
     */
    default <F> B whereInRaw(ColumnFunctionalInterface<T, F> column, String sql) {
        return whereInRaw(lambda2ColumnName(column), sql);
    }

    /**
     * 列值在范围内(子查询)
     * @param column 列名表达式
     * @param closure 闭包
     * @param <F> 属性类型
     * @return 查询构造器
     */
    default <F> B whereIn(ColumnFunctionalInterface<T, F> column,
        BuilderWrapper<B, T, K> closure) {
        return whereIn(lambda2ColumnName(column), closure);
    }

    /**
     * 列值不在范围内
     * @param column 列名表达式
     * @param valueList 值所在的list
     * @param <F> 属性类型
     * @return 查询构造器
     */
    default <F> B whereNotIn(ColumnFunctionalInterface<T, F> column, Collection<?> valueList) {
        return whereNotIn(lambda2ColumnName(column), valueList);
    }

    /**
     * 列值在范围内(忽略值为空的情况)
     * @param column 列名表达式
     * @param valueList 值所在的list
     * @param <F> 属性类型
     * @return 查询构造器
     */
    default <F> B whereNotInIgnoreEmpty(ColumnFunctionalInterface<T, F> column,
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
    default <F> B whereNotIn(ColumnFunctionalInterface<T, F> column, Object... valueArray) {
        return whereNotIn(lambda2ColumnName(column), valueArray);
    }

    /**
     * 列值在范围内(忽略值为空的情况)
     * @param column 列名表达式
     * @param valueArray 值所在的数组
     * @param <F> 属性类型
     * @return 查询构造器
     */
    default <F> B whereNotInIgnoreEmpty(ColumnFunctionalInterface<T, F> column,
        @Nullable Object... valueArray) {
        return whereNotInIgnoreEmpty(lambda2ColumnName(column), valueArray);
    }

    /**
     * 列值不在范围内(子查询)
     * @param column 列名表达式
     * @param sql 完整sql eg:select id from student where age>10
     * @param <F> 属性类型
     * @return 查询构造器
     */
    default <F> B whereNotInRaw(ColumnFunctionalInterface<T, F> column, String sql) {
        return whereNotInRaw(lambda2ColumnName(column), sql);
    }

    /**
     * 列值不在范围内(子查询)
     * @param column 列名表达式
     * @param closure 闭包
     * @param <F> 属性类型
     * @return 查询构造器
     */
    default <F> B whereNotIn(ColumnFunctionalInterface<T, F> column,
        BuilderWrapper<B, T, K> closure) {
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
    default <F> B whereBetween(ColumnFunctionalInterface<T, F> column, Object min, Object max) {
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
    default <F> B whereNotBetween(ColumnFunctionalInterface<T, F> column, Object min, Object max) {
        return whereNotBetween(lambda2ColumnName(column), min, max);
    }

    /**
     * 列值为null
     * @param column 列名表达式
     * @param <F> 属性类型
     * @return 查询构造器
     */
    default <F> B whereNull(ColumnFunctionalInterface<T, F> column) {
        return whereNull(lambda2ColumnName(column));
    }

    /**
     * 列值不为null
     * @param column 列名表达式
     * @param <F> 属性类型
     * @return 查询构造器
     */
    default <F> B whereNotNull(ColumnFunctionalInterface<T, F> column) {
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
    default <F> B whereColumn(ColumnFunctionalInterface<T, F> column1, String symbol,
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
    default <F> B whereColumn(ColumnFunctionalInterface<T, F> column1,
        ColumnFunctionalInterface<T, F> column2) {
        return whereColumn(lambda2ColumnName(column1), lambda2ColumnName(column2));
    }

    /**
     * 包含关联数据
     * @param relationFieldName 关系字段
     * @return 查询构造器
     */
    default <F> B whereHasSingle(ColumnFunctionalInterface<T, F> relationFieldName) {
        return whereHas(lambda2FieldName(relationFieldName), BuilderAnyWrapper.empty());
    }

    /**
     * 包含关联数据
     * @param relationFieldName 关系字段
     * @return 查询构造器
     */
    default <F> B whereHas(ColumnFunctionalInterface.ColumnCollection<T, F> relationFieldName) {
        return whereHas(lambda2FieldName(relationFieldName), BuilderAnyWrapper.empty());
    }

    /**
     * 包含关联数据
     * @param relationFieldName 关系字段
     * @return 查询构造器
     */
    default <F> B whereHas(ColumnFunctionalInterface.ColumnArray<T, F> relationFieldName) {
        return whereHas(lambda2FieldName(relationFieldName), BuilderAnyWrapper.empty());
    }

    /**
     * 包含关联数据
     * @param relationFieldName 关系字段
     * @param closure 闭包
     * @return 查询构造器
     */
    default <F> B whereHasSingle(ColumnFunctionalInterface<T, F> relationFieldName,
            BuilderEntityWrapper<F> closure) {
        return whereHas(lambda2FieldName(relationFieldName), BuilderAnyWrapper.turn2(closure));
    }

    /**
     * 包含关联数据
     * @param relationFieldName 关系字段
     * @param closure 闭包
     * @return 查询构造器
     */
    default <F> B whereHas(ColumnFunctionalInterface.ColumnCollection<T, F> relationFieldName,
            BuilderEntityWrapper<F> closure) {
        return whereHas(lambda2FieldName(relationFieldName), BuilderAnyWrapper.turn2(closure));
    }

    /**
     * 包含关联数据
     * @param relationFieldName 关系字段
     * @param closure 闭包
     * @return 查询构造器
     */
    default <F> B whereHas(ColumnFunctionalInterface.ColumnArray<T, F> relationFieldName,
        BuilderEntityWrapper<F> closure) {
        return whereHas(lambda2FieldName(relationFieldName), BuilderAnyWrapper.turn2(closure));
    }

    /**
     * 包含关联数据
     * @param relationFieldName 关系字段
     * @return 查询构造器
     */
    default <F> B whereNotHasSingle(ColumnFunctionalInterface<T, F> relationFieldName) {
        return whereNotHas(lambda2FieldName(relationFieldName), BuilderAnyWrapper.empty());
    }

    /**
     * 包含关联数据
     * @param relationFieldName 关系字段
     * @return 查询构造器
     */
    default <F> B whereNotHas(ColumnFunctionalInterface.ColumnArray<T, F> relationFieldName) {
        return whereNotHas(lambda2FieldName(relationFieldName), BuilderAnyWrapper.empty());
    }

    /**
     * 包含关联数据
     * @param relationFieldName 关系字段
     * @return 查询构造器
     */
    default <F> B whereNotHas(ColumnFunctionalInterface.ColumnCollection<T, F> relationFieldName) {
        return whereNotHas(lambda2FieldName(relationFieldName), BuilderAnyWrapper.empty());
    }

    /**
     * 包含关联数据
     * @param relationFieldName 关系字段
     * @param closure 闭包
     * @return 查询构造器
     */
    default <F> B whereNotHasSingle(ColumnFunctionalInterface<T, F> relationFieldName,
            BuilderEntityWrapper<F> closure) {
        return whereNotHas(lambda2FieldName(relationFieldName), BuilderAnyWrapper.turn2(closure));
    }

    /**
     * 包含关联数据
     * @param relationFieldName 关系字段
     * @param closure 闭包
     * @return 查询构造器
     */
    default <F> B whereNotHas(ColumnFunctionalInterface.ColumnArray<T, F> relationFieldName,
            BuilderEntityWrapper<F> closure) {
        return whereNotHas(lambda2FieldName(relationFieldName), BuilderAnyWrapper.turn2(closure));
    }

    /**
     * 包含关联数据
     * @param relationFieldName 关系字段
     * @param closure 闭包
     * @return 查询构造器
     */
    default <F> B whereNotHas(ColumnFunctionalInterface.ColumnCollection<T, F> relationFieldName,
        BuilderEntityWrapper<F> closure) {
        return whereNotHas(lambda2FieldName(relationFieldName), BuilderAnyWrapper.turn2(closure));
    }

    /**
     * 包含关联数据
     * @param relationFieldName 关系字段
     * @return 查询构造器
     */
    default <F> B whereHasInSingle(ColumnFunctionalInterface<T, F> relationFieldName) {
        return whereHasIn(lambda2FieldName(relationFieldName), BuilderAnyWrapper.empty());
    }

    /**
     * 包含关联数据
     * @param relationFieldName 关系字段
     * @return 查询构造器
     */
    default <F> B whereHasIn(ColumnFunctionalInterface.ColumnCollection<T, F> relationFieldName) {
        return whereHasIn(lambda2FieldName(relationFieldName), BuilderAnyWrapper.empty());
    }

    /**
     * 包含关联数据
     * @param relationFieldName 关系字段
     * @return 查询构造器
     */
    default <F> B whereHasIn(ColumnFunctionalInterface.ColumnArray<T, F> relationFieldName) {
        return whereHasIn(lambda2FieldName(relationFieldName), BuilderAnyWrapper.empty());
    }

    /**
     * 包含关联数据
     * @param relationFieldName 关系字段
     * @param closure 闭包
     * @return 查询构造器
     */
    default <F> B whereHasInSingle(ColumnFunctionalInterface<T, F> relationFieldName,
            BuilderEntityWrapper<F> closure) {
        return whereHasIn(lambda2FieldName(relationFieldName), BuilderAnyWrapper.turn2(closure));
    }

    /**
     * 包含关联数据
     * @param relationFieldName 关系字段
     * @param closure 闭包
     * @return 查询构造器
     */
    default <F> B whereHasIn(ColumnFunctionalInterface.ColumnCollection<T, F> relationFieldName,
            BuilderEntityWrapper<F> closure) {
        return whereHasIn(lambda2FieldName(relationFieldName), BuilderAnyWrapper.turn2(closure));
    }

    /**
     * 包含关联数据
     * @param relationFieldName 关系字段
     * @param closure 闭包
     * @return 查询构造器
     */
    default <F> B whereHasIn(ColumnFunctionalInterface.ColumnArray<T, F> relationFieldName,
            BuilderEntityWrapper<F> closure) {
        return whereHasIn(lambda2FieldName(relationFieldName), BuilderAnyWrapper.turn2(closure));
    }

    /**
     * 包含关联数据
     * @param relationFieldName 关系字段
     * @return 查询构造器
     */
    default <F> B whereNotHasInSingle(ColumnFunctionalInterface<T, F> relationFieldName) {
        return whereNotHasIn(lambda2FieldName(relationFieldName), BuilderAnyWrapper.empty());
    }

    /**
     * 包含关联数据
     * @param relationFieldName 关系字段
     * @return 查询构造器
     */
    default <F> B whereNotHasIn(ColumnFunctionalInterface.ColumnArray<T, F> relationFieldName) {
        return whereNotHasIn(lambda2FieldName(relationFieldName), BuilderAnyWrapper.empty());
    }

    /**
     * 包含关联数据
     * @param relationFieldName 关系字段
     * @return 查询构造器
     */
    default <F> B whereNotHasIn(ColumnFunctionalInterface.ColumnCollection<T, F> relationFieldName) {
        return whereNotHasIn(lambda2FieldName(relationFieldName), BuilderAnyWrapper.empty());
    }

    /**
     * 包含关联数据
     * @param relationFieldName 关系字段
     * @param closure 闭包
     * @return 查询构造器
     */
    default <F> B whereNotHasInSingle(ColumnFunctionalInterface<T, F> relationFieldName,
            BuilderEntityWrapper<F> closure) {
        return whereNotHasIn(lambda2FieldName(relationFieldName), BuilderAnyWrapper.turn2(closure));
    }

    /**
     * 包含关联数据
     * @param relationFieldName 关系字段
     * @param closure 闭包
     * @return 查询构造器
     */
    default <F> B whereNotHasIn(ColumnFunctionalInterface.ColumnArray<T, F> relationFieldName,
            BuilderEntityWrapper<F> closure) {
        return whereNotHasIn(lambda2FieldName(relationFieldName), BuilderAnyWrapper.turn2(closure));
    }

    /**
     * 包含关联数据
     * @param relationFieldName 关系字段
     * @param closure 闭包
     * @return 查询构造器
     */
    default <F> B whereNotHasIn(ColumnFunctionalInterface.ColumnCollection<T, F> relationFieldName,
            BuilderEntityWrapper<F> closure) {
        return whereNotHasIn(lambda2FieldName(relationFieldName), BuilderAnyWrapper.turn2(closure));
    }
}
