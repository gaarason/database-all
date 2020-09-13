package gaarason.database.contract.builder;

import gaarason.database.contract.function.GenerateSqlPart;
import gaarason.database.query.Builder;

import java.util.Collection;

/**
 * 查询后过滤
 * @param <T>
 * @param <K>
 */
public interface Having<T, K> {

    /**
     * 加入sql片段
     * @param sqlPart sql片段
     * @return 查询构建器
     */
    Builder<T, K> havingRaw(String sqlPart);

    /**
     * 比较列与值
     * @param column 列名
     * @param symbol 比较关系
     * @param value  值
     * @return 查询构建器
     */
    Builder<T, K> having(String column, String symbol, String value);

    /**
     * 比较列与值相等
     * @param column 列名
     * @param value  值
     * @return 查询构建器
     */
    Builder<T, K> having(String column, String value);

    /**
     * 列值在范围内
     * @param column    列名
     * @param valueList 值所在的list
     * @return 查询构建器
     */
    Builder<T, K> havingIn(String column, Collection<Object> valueList);

    /**
     * 列值在范围内
     * @param column     列名
     * @param valueArray 值所在的数组
     * @return 查询构建器
     */
    Builder<T, K> havingIn(String column, String... valueArray);

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
    Builder<T, K> havingIn(String column, GenerateSqlPart closure);

    /**
     * 列值不在范围内
     * @param column    列名
     * @param valueList 值所在的list
     * @return 查询构建器
     */
    Builder<T, K> havingNotIn(String column, Collection<Object> valueList);

    /**
     * 列值在范围内
     * @param column     列名
     * @param valueArray 值所在的数组
     * @return 查询构建器
     */
    Builder<T, K> havingNotIn(String column, String... valueArray);

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
    Builder<T, K> havingNotIn(String column, GenerateSqlPart closure);

    /**
     * 列值在2值之间
     * @param column 列名
     * @param min    值1
     * @param max    值2
     * @return 查询构建器
     */
    Builder<T, K> havingBetween(String column, String min, String max);

    /**
     * 列值不在2值之间
     * @param column 列名
     * @param min    值1
     * @param max    值2
     * @return 查询构建器
     */
    Builder<T, K> havingNotBetween(String column, String min, String max);

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
     * @param Closure 闭包
     * @return 查询构建器
     */
    Builder<T, K> havingExists(GenerateSqlPart Closure);

    /**
     * not exists一个闭包
     * @param sql 闭包
     * @return 查询构建器
     */
    Builder<T, K> havingNotExistsRaw(String sql);

    /**
     * not exists一个完整sql
     * @param Closure 完整sql
     * @return 查询构建器
     */
    Builder<T, K> havingNotExists(GenerateSqlPart Closure);

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
    Builder<T, K> andHaving(GenerateSqlPart closure);

    /**
     * 或
     * @param closure 闭包
     * @return 查询构建器
     */
    Builder<T, K> orHaving(GenerateSqlPart closure);
}
