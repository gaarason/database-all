package gaarason.database.contract.builder;

import gaarason.database.contract.eloquent.Builder;
import gaarason.database.contract.function.GenerateSqlPartFunctionalInterface;
import gaarason.database.lang.Nullable;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

/**
 * 查询后过滤
 * @param <T>
 * @param <K>
 * @author xt
 */
public interface Having<T extends Serializable, K extends Serializable> {

    /**
     * 加入sql片段
     * @param sqlPart sql片段
     * @return 查询构建器
     */
    Builder<T, K> havingRaw(@Nullable String sqlPart);

    /**
     * 加入sql片段集合
     * @param sqlParts sql片段集合
     * @return 查询构建器
     */
    Builder<T, K> havingRaw(@Nullable Collection<String> sqlParts);

    /**
     * 比较列与值
     * @param column 列名
     * @param symbol 比较关系
     * @param value  值
     * @return 查询构建器
     */
    Builder<T, K> having(String column, String symbol, Object value);

    /**
     * 比较列与值(忽略值为null的情况)
     * @param column 列名
     * @param symbol 比较关系
     * @param value  值
     * @return 查询构建器
     */
    Builder<T, K> havingIgnoreNull(String column, String symbol, @Nullable Object value);

    /**
     * 比较列与值相等
     * @param column 列名
     * @param value  值
     * @return 查询构建器
     */
    Builder<T, K> having(String column, @Nullable Object value);

    /**
     * 比较列与值相等(忽略值为null的情况)
     * @param column 列名
     * @param value  值
     * @return 查询构建器
     */
    Builder<T, K> havingIgnoreNull(String column, @Nullable Object value);

    /**
     * 将对象的属性转化为, 列与值相等的查询条件
     * @param entity 实体对象
     * @return 查询构建器
     */
    Builder<T, K> having(T entity);

    /**
     * 列与值相等的查询条件
     * @param map 条件map
     * @return 查询构建器
     */
    Builder<T, K> having(Map<String, Object> map);

    /**
     * 列与值相等的查询条件(忽略MAP中，值为null的情况)
     * @param map 条件map
     * @return 查询构建器
     */
    Builder<T, K> havingIgnoreNull(@Nullable Map<String, Object> map);

    /**
     * "列like值" 的查询条件, 其中值需要自行包含 % 符号 (忽略值为null的情况)
     * @param column 列名
     * @param value  值
     * @return 查询构建器
     */
    Builder<T, K> havingLike(String column, @Nullable Object value);

    /**
     * 将对象的属性转化为, "列like值" 的查询条件, 其中值需要自行包含 % 符号 (忽略entity中，值为null的情况)
     * @param entity 实体对象
     * @return 查询构建器
     */
    Builder<T, K> havingLike(@Nullable T entity);

    /**
     * "列like值" 的查询条件, 其中值需要自行包含 % 符号, (忽略MAP中，值为null的情况)
     * @param map 条件map
     * @return 查询构建器
     */
    Builder<T, K> havingLike(@Nullable Map<String, Object> map);

    /**
     * 选择可能的条件类型
     * 当 value 以 %开头或者结尾时, 使用like查询
     * 当 value 为 null 时, 使用 is null 查询
     * 其他情况下, 使用 = 查询
     * @param column 列名
     * @param value  值
     * @return 查询构建器
     */
    Builder<T, K> havingMayLike(String column, @Nullable Object value);

    /**
     * 选择可能的条件类型
     * 当 value 以 %开头或者结尾时, 使用like查询
     * 当 value 为 null 时, 当 @Column中的nullable=true ,使用 is null 查询
     * 当 value 为 null 时, 当 @Column中的nullable=false, 忽略
     * 其他情况下, 使用 = 查询
     * @param entity 实体对象
     * @return 查询构建器
     */
    Builder<T, K> havingMayLike(@Nullable T entity);

    /**
     * 选择可能的条件类型
     * 当 value 以 %开头或者结尾时, 使用like查询
     * 当 value 为 null 时, 忽略
     * 其他情况下, 使用 = 查询
     * @param map 条件map
     * @return 查询构建器
     */
    Builder<T, K> havingMayLike(@Nullable Map<String, Object> map);

    /**
     * 列值在范围内
     * @param column    列名
     * @param valueList 值所在的list
     * @return 查询构建器
     */
    Builder<T, K> havingIn(String column, Collection<?> valueList);

    /**
     * 列值在范围内(忽略值为空的情况)
     * @param column    列名
     * @param valueList 值所在的list
     * @return 查询构建器
     */
    Builder<T, K> havingInIgnoreEmpty(String column, @Nullable Collection<?> valueList);

    /**
     * 列值在范围内
     * @param column     列名
     * @param valueArray 值所在的数组
     * @return 查询构建器
     */
    Builder<T, K> havingIn(String column, Object... valueArray);

    /**
     * 列值在范围内(忽略值为空的情况)
     * @param column     列名
     * @param valueArray 值所在的数组
     * @return 查询构建器
     */
    Builder<T, K> havingInIgnoreEmpty(String column, @Nullable Object... valueArray);

    /**
     * 列值在范围内(子查询)
     * @param column 列名
     * @param sql    完整sql eg:select id from student having age>10
     * @return 查询构建器
     */
    Builder<T, K> havingInRaw(String column, String sql);

    /**
     * 列值在范围内(子查询)
     * @param column  列名
     * @param closure 闭包
     * @return 查询构建器
     */
    Builder<T, K> havingIn(String column, GenerateSqlPartFunctionalInterface<T, K> closure);

    /**
     * 列值不在范围内
     * @param column    列名
     * @param valueList 值所在的list
     * @return 查询构建器
     */
    Builder<T, K> havingNotIn(String column, Collection<?> valueList);

    /**
     * 列值不在范围内(忽略值为空的情况)
     * @param column    列名
     * @param valueList 值所在的list
     * @return 查询构建器
     */
    Builder<T, K> havingNotInIgnoreEmpty(String column, @Nullable Collection<?> valueList);

    /**
     * 列值在范围内
     * @param column     列名
     * @param valueArray 值所在的数组
     * @return 查询构建器
     */
    Builder<T, K> havingNotIn(String column, Object... valueArray);

    /**
     * 列值在范围内(忽略值为空的情况)
     * @param column     列名
     * @param valueArray 值所在的数组
     * @return 查询构建器
     */
    Builder<T, K> havingNotInIgnoreEmpty(String column, @Nullable Object... valueArray);

    /**
     * 列值不在范围内(子查询)
     * @param column 列名
     * @param sql    完整sql eg:select id from student having age>10
     * @return 查询构建器
     */
    Builder<T, K> havingNotInRaw(String column, String sql);

    /**
     * 列值不在范围内(子查询)
     * @param column  列名
     * @param closure 闭包
     * @return 查询构建器
     */
    Builder<T, K> havingNotIn(String column, GenerateSqlPartFunctionalInterface<T, K> closure);

    /**
     * 列值在2值之间
     * @param column 列名
     * @param min    值1
     * @param max    值2
     * @return 查询构建器
     */
    Builder<T, K> havingBetween(String column, Object min, Object max);

    /**
     * 列值不在2值之间
     * @param column 列名
     * @param min    值1
     * @param max    值2
     * @return 查询构建器
     */
    Builder<T, K> havingNotBetween(String column, Object min, Object max);

    /**
     * 列值为null
     * @param column 列名
     * @return 查询构建器
     */
    Builder<T, K> havingNull(String column);

    /**
     * 列值不为null
     * @param column 列名
     * @return 查询构建器
     */
    Builder<T, K> havingNotNull(String column);

    /**
     * exists一个sql
     * @param sql 完整sql
     * @return 查询构建器
     */
    Builder<T, K> havingExistsRaw(String sql);

    /**
     * exists一个闭包
     * @param closure 闭包
     * @return 查询构建器
     */
    Builder<T, K> havingExists(GenerateSqlPartFunctionalInterface<T, K> closure);

    /**
     * not exists一个闭包
     * @param sql 闭包
     * @return 查询构建器
     */
    Builder<T, K> havingNotExistsRaw(String sql);

    /**
     * not exists一个完整sql
     * @param closure 完整sql
     * @return 查询构建器
     */
    Builder<T, K> havingNotExists(GenerateSqlPartFunctionalInterface<T, K> closure);

    /**
     * 比较字段与字段
     * @param column1 列1
     * @param symbol  比较关系
     * @param column2 列2
     * @return 查询构建器
     */
    Builder<T, K> havingColumn(String column1, String symbol, String column2);

    /**
     * 字段与字段相等
     * @param column1 列1
     * @param column2 列2
     * @return 查询构建器
     */
    Builder<T, K> havingColumn(String column1, String column2);

    /**
     * 且
     * @param closure 闭包
     * @return 查询构建器
     */
    Builder<T, K> andHaving(GenerateSqlPartFunctionalInterface<T, K> closure);

    /**
     * 或
     * @param closure 闭包
     * @return 查询构建器
     */
    Builder<T, K> orHaving(GenerateSqlPartFunctionalInterface<T, K> closure);
}
