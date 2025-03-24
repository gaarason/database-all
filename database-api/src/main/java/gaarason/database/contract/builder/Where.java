package gaarason.database.contract.builder;

import gaarason.database.contract.eloquent.Builder;
import gaarason.database.contract.function.BuilderAnyWrapper;
import gaarason.database.contract.function.BuilderWrapper;
import gaarason.database.lang.Nullable;

import java.util.Collection;
import java.util.Map;

/**
 * 条件
 * @param <T>
 * @param <K>
 * @author xt
 */
public interface Where<B extends Builder<B, T, K>, T, K> {

    /**
     * 加入sql片段
     * @param sqlPart sql片段
     * @param parameters 参数绑定列表
     * @return 查询构造器
     */
    B whereRaw(@Nullable String sqlPart, @Nullable Collection<?> parameters);

    /**
     * 加入sql片段
     * @param sqlPart sql片段
     * @return 查询构造器
     */
    B whereRaw(@Nullable String sqlPart);

    /**
     * 加入sql片段集合
     * @param sqlParts sql片段集合
     * @return 查询构造器
     */
    B whereRaw(@Nullable Collection<String> sqlParts);

    /**
     * 比较列与值
     * @param column 列名
     * @param symbol 比较关系
     * @param value 值
     * @return 查询构造器
     */
    B where(String column, String symbol, Object value);

    /**
     * 列包含选项值
     * @param column 列名(位存储)
     * @param value 选项值(eg: 0,1,2,3)
     * @return 查询构造器
     */
    B whereBit(String column, Object value);

    /**
     * 列不包含选项值
     * @param column 列名(位存储)
     * @param value 选项值(eg: 0,1,2,3)
     * @return 查询构造器
     */
    B whereBitNot(String column, Object value);

    /**
     * 列包含选项值其一
     * @param column 列名(位存储)
     * @param values 选项值(eg: 0,1,2,3)
     * @return 查询构造器
     */
    B whereBitIn(String column, Collection<?> values);

    /**
     * 列不包含选项值其一
     * @param column 列名(位存储)
     * @param values 选项值(eg: 0,1,2,3)
     * @return 查询构造器
     */
    B whereBitNotIn(String column, Collection<?> values);

    /**
     * 列完全包含所有选项值
     * @param column 列名(位存储)
     * @param values 选项值(eg: 0,1,2,3)
     * @return 查询构造器
     */
    B whereBitStrictIn(String column, Collection<?> values);

    /**
     * 列完全不包含所有选项值
     * @param column 列名(位存储)
     * @param values 选项值(eg: 0,1,2,3)
     * @return 查询构造器
     */
    B whereBitStrictNotIn(String column, Collection<?> values);

    /**
     * 比较列与值(忽略值为null的情况)
     * @param column 列名
     * @param symbol 比较关系
     * @param value 值
     * @return 查询构造器
     */
    B whereIgnoreNull(String column, String symbol, @Nullable Object value);

    /**
     * 比较列与值相等
     * @param column 列名
     * @param value 值
     * @return 查询构造器
     */
    B where(String column, @Nullable Object value);

    /**
     * 比较列与值相等(忽略值为null的情况)
     * @param column 列名
     * @param value 值
     * @return 查询构造器
     */
    B whereIgnoreNull(String column, @Nullable Object value);

    /**
     * 将对象的属性转化为, 列与值相等的查询条件
     * @param anyEntity 非预定义的实体对象
     * @return 查询构造器
     */
    B where(Object anyEntity);

    /**
     * 列与值相等的查询条件
     * @param map 条件map
     * @return 查询构造器
     */
    B where(Map<String, Object> map);

    /**
     * 使用组合条件
     * 当 value 是集合类型时, 使用 in 查询
     * 当 value 是MAP类型，且存在start以及end时，使用 between 查询
     * 当 value 以 %开头或者结尾时, 使用 like查询
     * 当 value 为 null 时, 使用 is null
     * 其他情况下, 使用 = 查询
     * @param map 条件map
     * @return 查询构造器
     */
    B whereFind(@Nullable Map<String, Object> map);

    /**
     * 使用组合条件
     * 当 value 是集合类型时, 使用 not in 查询
     * 当 value 是MAP类型，且存在start以及end时，使用 not between 查询
     * 当 value 以 %开头或者结尾时, 使用 not like查询
     * 当 value 为 null 时, 使用 is not null
     * 其他情况下, 使用 != 查询
     * @param map 条件map
     * @return 查询构造器
     */
    B whereNotFind(@Nullable Map<String, Object> map);

    /**
     * 列与值相等的查询条件(忽略MAP中，值为null的情况)
     * @param map 条件map
     * @return 查询构造器
     */
    B whereIgnoreNull(@Nullable Map<String, Object> map);

    /**
     * 在多个列中, 查找值, 任一满足
     * 忽略 value 为 null 、"" 、% 、%% 的情况
     * 其中值如果没有在开头或结尾自行包含 % 符号，则在开头以及结尾拼接 % 符号
     * sql eg : where ( column1 like %value% or column2 like %value% )
     * @param value 值
     * @param columns 列名集合
     * @return 查询构造器
     */
    B whereAnyLike(@Nullable Object value, Collection<String> columns);

    /**
     * 在多个列中, 查找值, 任一满足
     * 忽略 value 为 null 、"" 、% 、%% 的情况
     * 其中值如果没有在开头或结尾自行包含 % 符号，则在开头以及结尾拼接 % 符号
     * sql eg : where ( column1 like %value% or column2 like %value% )
     * @param value 值
     * @param columns 列名
     * @return 查询构造器
     */
    B whereAnyLike(@Nullable Object value, String... columns);

    /**
     * 在多个列中, 查找值, 全部满足
     * 忽略 value 为 null 、"" 、% 、%% 的情况
     * 其中值如果没有在开头或结尾自行包含 % 符号，则在开头以及结尾拼接 % 符号
     * sql eg : where ( column1 like %value% and column2 like %value% )
     * @param value 值
     * @param columns 列名集合
     * @return 查询构造器
     */
    B whereAllLike(@Nullable Object value, Collection<String> columns);

    /**
     * 在多个列中, 查找值, 全部满足
     * 忽略 value 为 null 、"" 、% 、%% 的情况
     * 其中值如果没有在开头或结尾自行包含 % 符号，则在开头以及结尾拼接 % 符号
     * sql eg : where ( column1 like %value% and column2 like %value% )
     * @param value 值
     * @param columns 列名
     * @return 查询构造器
     */
    B whereAllLike(@Nullable Object value, String... columns);

    /**
     * "列like值" 的查询条件
     * 忽略 value 为 null 、"" 、% 、%% 的情况
     * 其中值如果没有在开头或结尾自行包含 % 符号，则在开头以及结尾拼接 % 符号
     * @param column 列名
     * @param value 值
     * @return 查询构造器
     */
    B whereLike(String column, @Nullable Object value);

    /**
     * 将对象的属性转化为, "列like值" 的查询条件
     * 忽略 value 为 null 、"" 、% 、%% 的情况
     * 其中值如果没有在开头或结尾自行包含 % 符号，则在开头以及结尾拼接 % 符号
     * @param anyEntity 实体对象
     * @return 查询构造器
     */
    B whereLike(@Nullable Object anyEntity);

    /**
     * "列like值" 的查询条件
     * 忽略 value 为 null 、"" 、% 、%% 的情况
     * 其中值如果没有在开头或结尾自行包含 % 符号，则在开头以及结尾拼接 % 符号
     * @param map 条件map
     * @return 查询构造器
     */
    B whereLike(@Nullable Map<String, Object> map);

    /**
     * "列 not like值" 的查询条件
     * 忽略 value 为 null 、"" 、% 、%% 的情况
     * 其中值如果没有在开头或结尾自行包含 % 符号，则在开头以及结尾拼接 % 符号
     * @param column 列名
     * @param value 值
     * @return 查询构造器
     */
    B whereNotLike(String column, @Nullable Object value);

    /**
     * 将对象的属性转化为, "列 not like值" 的查询条件
     * 忽略 value 为 null 、"" 、% 、%% 的情况
     * 其中值如果没有在开头或结尾自行包含 % 符号，则在开头以及结尾拼接 % 符号
     * @param anyEntity 实体对象
     * @return 查询构造器
     */
    B whereNotLike(@Nullable Object anyEntity);

    /**
     * "列 not like值" 的查询条件
     * 忽略 value 为 null 、"" 、% 、%% 的情况
     * 其中值如果没有在开头或结尾自行包含 % 符号，则在开头以及结尾拼接 % 符号
     * @param map 条件map
     * @return 查询构造器
     */
    B whereNotLike(@Nullable Map<String, Object> map);

    /**
     * 选择可能的条件类型
     * 忽略 value 为 % 、%% 的情况
     * 当 value 以 %开头或者结尾时, 使用like查询
     * 当 value 为 null 时, 使用 is null 查询
     * 其他情况下, 使用 = 查询
     * @param column 列名
     * @param value 值
     * @return 查询构造器
     */
    B whereMayLike(String column, @Nullable Object value);

    /**
     * 选择可能的条件类型
     * 忽略 value 为 % 、%% 的情况
     * 当 value 以 %开头或者结尾时, 使用not like查询
     * 当 value 为 null 时, 使用 is not null 查询
     * 其他情况下, 使用 != 查询
     * @param column 列名
     * @param value 值
     * @return 查询构造器
     */
    B whereMayNotLike(String column, @Nullable Object value);

    /**
     * 选择可能的条件类型
     * 忽略 value 为 null 、% 、%% 的情况
     * 当 value 以 %开头或者结尾时, 使用like查询
     * 其他情况下, 使用 = 查询
     * @param column 列名
     * @param value 值
     * @return 查询构造器
     */
    B whereMayLikeIgnoreNull(String column, @Nullable Object value);

    /**
     * 选择可能的条件类型
     * 忽略 value 为 null 、% 、%% 的情况
     * 当 value 以 %开头或者结尾时, 使用not like查询
     * 其他情况下, 使用 != 查询
     * @param column 列名
     * @param value 值
     * @return 查询构造器
     */
    B whereMayNotLikeIgnoreNull(String column, @Nullable Object value);

    /**
     * 选择可能的条件类型
     * 当 value 为 % 、%% 时, 忽略
     * 当 value 以 %开头或者结尾时, 使用like查询
     * 当 value 为 null 时, 使用 is null 查询
     * 其他情况下, 使用 = 查询
     * @param anyEntity 实体对象
     * @return 查询构造器
     */
    B whereMayLike(@Nullable Object anyEntity);

    /**
     * 选择可能的条件类型
     * 当 value 为 % 、%% 时, 忽略
     * 当 value 以 %开头或者结尾时, 使用not like查询
     * 当 value 为 null 时, 使用 is not null 查询
     * 其他情况下, 使用 != 查询
     * @param anyEntity 实体对象
     * @return 查询构造器
     */
    B whereMayNotLike(@Nullable Object anyEntity);

    /**
     * 选择可能的条件类型
     * 当 value 为 % 、%% 时, 忽略
     * 当 value 以 %开头或者结尾时, 使用like查询
     * 当 value 为 null 时, 使用 is null 查询
     * 其他情况下, 使用 = 查询
     * @param map 条件map
     * @return 查询构造器
     */
    B whereMayLike(@Nullable Map<String, Object> map);

    /**
     * 选择可能的条件类型
     * 当 value 为 % 、%% 时, 忽略
     * 当 value 以 %开头或者结尾时, 使用not like查询
     * 当 value 为 null 时, 使用 is not null 查询
     * 其他情况下, 使用 != 查询
     * @param map 条件map
     * @return 查询构造器
     */
    B whereMayNotLike(@Nullable Map<String, Object> map);

    /**
     * 选择可能的条件类型
     * 当 value 为 null, %, %% 时, 忽略
     * 当 value 以 %开头或者结尾时, 使用like查询
     * 其他情况下, 使用 = 查询
     * @param map 条件map
     * @return 查询构造器
     */
    B whereMayLikeIgnoreNull(@Nullable Map<String, Object> map);

    /**
     * 选择可能的条件类型
     * 当 value 为 null 、% 、%% 时, 忽略
     * 当 value 以 %开头或者结尾时, 使用not like查询
     * 其他情况下, 使用 != 查询
     * @param map 条件map
     * @return 查询构造器
     */
    B whereMayNotLikeIgnoreNull(@Nullable Map<String, Object> map);

    /**
     * 条件子查询
     * @param column 列名
     * @param symbol 关系符号
     * @param completeSql 完整sql
     * @return 查询构造器
     */
    B whereSubQuery(String column, String symbol, String completeSql);

    /**
     * 条件子查询
     * @param column 列名
     * @param symbol 关系符号
     * @param closure 闭包
     * @return 查询构造器
     */
    B whereSubQuery(String column, String symbol, BuilderWrapper<B, T, K> closure);

    /**
     * 列值在范围内
     * @param column 列名
     * @param valueList 值所在的list
     * @return 查询构造器
     */
    B whereIn(String column, Collection<?> valueList);

    /**
     * 列值在范围内(忽略值为空的情况)
     * @param column 列名
     * @param valueList 值所在的list
     * @return 查询构造器
     */
    B whereInIgnoreEmpty(String column, @Nullable Collection<?> valueList);

    /**
     * 列值在范围内
     * @param column 列名
     * @param valueArray 值所在的数组
     * @return 查询构造器
     */
    B whereIn(String column, Object... valueArray);

    /**
     * 列值在范围内(忽略值为空的情况)
     * @param column 列名
     * @param valueArray 值所在的数组
     * @return 查询构造器
     */
    B whereInIgnoreEmpty(String column, @Nullable Object... valueArray);

    /**
     * 列值在范围内(子查询)
     * @param column 列名
     * @param sql 完整sql eg:select id from student where age>10
     * @return 查询构造器
     */
    B whereInRaw(String column, String sql);

    /**
     * 列值在范围内(子查询)
     * @param column 列名
     * @param closure 闭包
     * @return 查询构造器
     */
    B whereIn(String column, BuilderWrapper<B, T, K> closure);

    /**
     * 列值不在范围内
     * @param column 列名
     * @param valueList 值所在的list
     * @return 查询构造器
     */
    B whereNotIn(String column, Collection<?> valueList);

    /**
     * 列值在范围内(忽略值为空的情况)
     * @param column 列名
     * @param valueList 值所在的list
     * @return 查询构造器
     */
    B whereNotInIgnoreEmpty(String column, @Nullable Collection<?> valueList);

    /**
     * 列值在范围内
     * @param column 列名
     * @param valueArray 值所在的数组
     * @return 查询构造器
     */
    B whereNotIn(String column, Object... valueArray);

    /**
     * 列值在范围内(忽略值为空的情况)
     * @param column 列名
     * @param valueArray 值所在的数组
     * @return 查询构造器
     */
    B whereNotInIgnoreEmpty(String column, @Nullable Object... valueArray);

    /**
     * 列值不在范围内(子查询)
     * @param column 列名
     * @param sql 完整sql eg:select id from student where age>10
     * @return 查询构造器
     */
    B whereNotInRaw(String column, String sql);

    /**
     * 列值不在范围内(子查询)
     * @param column 列名
     * @param closure 闭包
     * @return 查询构造器
     */
    B whereNotIn(String column, BuilderWrapper<B, T, K> closure);

    /**
     * 列值在两值之间
     * @param column 列名
     * @param min 值1
     * @param max 值2
     * @return 查询构造器
     */
    B whereBetween(String column, Object min, Object max);

    /**
     * 列值在两值之间
     * @param column 列名
     * @param min 值1
     * @param max 值2
     * @param parameters 参数绑定列表
     * @return 查询构造器
     */
    B whereBetweenRaw(String column, Object min, Object max, @Nullable Collection<?> parameters);

    /**
     * 列值在两值之间
     * @param column 列名
     * @param min 值1
     * @param max 值2
     * @return 查询构造器
     */
    B whereBetweenRaw(String column, Object min, Object max);

    /**
     * 列值不在两值之间
     * @param column 列名
     * @param min 值1
     * @param max 值2
     * @return 查询构造器
     */
    B whereNotBetween(String column, Object min, Object max);

    /**
     * 列值不在两值之间
     * @param column 列名
     * @param min 值1
     * @param max 值2
     * @param parameters 参数绑定列表
     * @return 查询构造器
     */
    B whereNotBetweenRaw(String column, Object min, Object max, @Nullable Collection<?> parameters);

    /**
     * 列值不在两值之间
     * @param column 列名
     * @param min 值1
     * @param max 值2
     * @return 查询构造器
     */
    B whereNotBetweenRaw(String column, Object min, Object max);

    /**
     * 列值为null
     * @param column 列名
     * @return 查询构造器
     */
    B whereNull(String column);

    /**
     * 列值不为null
     * @param column 列名
     * @return 查询构造器
     */
    B whereNotNull(String column);

    /**
     * exists一个sql
     * @param sql 完整sql
     * @return 查询构造器
     */
    B whereExistsRaw(String sql);

    /**
     * exists一个闭包
     * @param closure 闭包
     * @return 查询构造器
     */
    B whereExists(BuilderWrapper<B, T, K> closure);

    /**
     * exists一个闭包
     * @param closure 闭包
     * @return 查询构造器
     */
    B whereAnyExists(BuilderAnyWrapper closure);

    /**
     * not exists一个闭包
     * @param sql 闭包
     * @return 查询构造器
     */
    B whereNotExistsRaw(String sql);

    /**
     * not exists一个完整sql
     * @param closure 完整sql
     * @return 查询构造器
     */
    B whereNotExists(BuilderWrapper<B, T, K> closure);

    /**
     * not exists一个完整sql
     * @param closure 完整sql
     * @return 查询构造器
     */
    B whereAnyNotExists(BuilderAnyWrapper closure);

    /**
     * 比较字段与字段
     * @param column1 列1
     * @param symbol 比较关系
     * @param column2 列2
     * @return 查询构造器
     */
    B whereColumn(String column1, String symbol, String column2);

    /**
     * 字段与字段相等
     * @param column1 列1
     * @param column2 列2
     * @return 查询构造器
     */
    B whereColumn(String column1, String column2);

    /**
     * 否定
     * @param closure 闭包
     * @return 查询构造器
     */
    B whereNot(BuilderWrapper<B, T, K> closure);

    /**
     * 且
     * @param closure 闭包
     * @return 查询构造器
     */
    B andWhere(BuilderWrapper<B, T, K> closure);

    /**
     * 且, 忽略空语句
     * @param closure 闭包
     * @return 查询构造器
     */
    B andWhereIgnoreEmpty(BuilderWrapper<B, T, K> closure);

    /**
     * 或
     * @param closure 闭包
     * @return 查询构造器
     */
    B orWhere(BuilderWrapper<B, T, K> closure);

    /**
     * 或, 忽略空语句
     * @param closure 闭包
     * @return 查询构造器
     */
    B orWhereIgnoreEmpty(BuilderWrapper<B, T, K> closure);

    /**
     * 包含关联数据
     * @param relationFieldName 关系字段
     * @return 查询构造器
     */
    default B whereHas(String relationFieldName) {
        return whereHas(relationFieldName, BuilderAnyWrapper.empty());
    }

    /**
     * 包含关联数据
     * @param relationFieldName 关系字段
     * @param closure 闭包
     * @return 查询构造器
     */
    B whereHas(String relationFieldName, BuilderAnyWrapper closure);

    /**
     * 包含关联数据
     * @param relationFieldName 关系字段
     * @return 查询构造器
     */
    default B whereNotHas(String relationFieldName) {
        return whereNotHas(relationFieldName, BuilderAnyWrapper.empty());
    }

    /**
     * 包含关联数据
     * @param relationFieldName 关系字段
     * @param closure 闭包
     * @return 查询构造器
     */
    B whereNotHas(String relationFieldName, BuilderAnyWrapper closure);

    /**
     * 包含关联数据
     * @param relationFieldName 关系字段
     * @return 查询构造器
     */
    default B whereHasIn(String relationFieldName) {
        return whereHasIn(relationFieldName, BuilderAnyWrapper.empty());
    }

    /**
     * 包含关联数据
     * @param relationFieldName 关系字段
     * @param closure 闭包
     * @return 查询构造器
     */
    B whereHasIn(String relationFieldName, BuilderAnyWrapper closure);

    /**
     * 包含关联数据
     * @param relationFieldName 关系字段
     * @return 查询构造器
     */
    default B whereNotHasIn(String relationFieldName) {
        return whereNotHasIn(relationFieldName, BuilderAnyWrapper.empty());
    }

    /**
     * 包含关联数据
     * @param relationFieldName 关系字段
     * @param closure 闭包
     * @return 查询构造器
     */
    B whereNotHasIn(String relationFieldName, BuilderAnyWrapper closure);
}
