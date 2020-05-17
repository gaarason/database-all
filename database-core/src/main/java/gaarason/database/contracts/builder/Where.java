package gaarason.database.contracts.builder;

import gaarason.database.contracts.function.GenerateSqlPart;
import gaarason.database.query.Builder;

import java.util.Collection;

/**
 * 条件
 * @param <T>
 * @param <K>
 */
public interface Where<T, K> {

    /**
     * 加入sql片段
     * @param sqlPart sql片段
     * @return 查询构建器
     */
    Builder<T, K> whereRaw(String sqlPart);

    /**
     * 比较列与值
     * @param column 列名
     * @param symbol 比较关系
     * @param value  值
     * @return 查询构建器
     */
    Builder<T, K> where(String column, String symbol, String value);

    /**
     * 比较列与值相等
     * @param column 列名
     * @param value  值
     * @return 查询构建器
     */
    Builder<T, K> where(String column, String value);

    /**
     * 条件子查询
     * @param column      列名
     * @param symbol      关系符号
     * @param completeSql 完整sql
     * @return 查询构建器
     */
    Builder<T, K> whereSubQuery(String column, String symbol, String completeSql);

    /**
     * 条件子查询
     * @param column  列名
     * @param symbol  关系符号
     * @param closure 闭包
     * @return 查询构建器
     */
    Builder<T, K> whereSubQuery(String column, String symbol, GenerateSqlPart<T, K> closure);

    /**
     * 列值在范围内
     * @param column    列名
     * @param valueList 值所在的list
     * @return 查询构建器
     */
    Builder<T, K> whereIn(String column, Collection<Object> valueList);

    /**
     * 列值在范围内(子查询)
     * @param column 列名
     * @param sql    完整sql eg:select id from student where age>10
     * @return 查询构建器
     */
    Builder<T, K> whereInRaw(String column, String sql);

    /**
     * 列值在范围内(子查询)
     * @param column  列名
     * @param closure 闭包
     * @return 查询构建器
     */
    Builder<T, K> whereIn(String column, GenerateSqlPart<T, K> closure);

    /**
     * 列值不在范围内
     * @param column    列名
     * @param valueList 值所在的list
     * @return 查询构建器
     */
    Builder<T, K> whereNotIn(String column, Collection<Object> valueList);

    /**
     * 列值不在范围内(子查询)
     * @param column 列名
     * @param sql    完整sql eg:select id from student where age>10
     * @return 查询构建器
     */
    Builder<T, K> whereNotInRaw(String column, String sql);

    /**
     * 列值不在范围内(子查询)
     * @param column  列名
     * @param closure 闭包
     * @return 查询构建器
     */
    Builder<T, K> whereNotIn(String column, GenerateSqlPart<T, K> closure);

    /**
     * 列值在2值之间
     * @param column 列名
     * @param min    值1
     * @param max    值2
     * @return 查询构建器
     */
    Builder<T, K> whereBetween(String column, String min, String max);

    /**
     * 列值不在2值之间
     * @param column 列名
     * @param min    值1
     * @param max    值2
     * @return 查询构建器
     */
    Builder<T, K> whereNotBetween(String column, String min, String max);

    /**
     * 列值为null
     * @param column 列名
     * @return 查询构建器
     */
    Builder<T, K> whereNull(String column);

    /**
     * 列值不为null
     * @param column 列名
     * @return 查询构建器
     */
    Builder<T, K> whereNotNull(String column);

    /**
     * exists一个sql
     * @param sql 完整sql
     * @return 查询构建器
     */
    Builder<T, K> whereExistsRaw(String sql);

    /**
     * exists一个闭包
     * @param closure 闭包
     * @return 查询构建器
     */
    Builder<T, K> whereExists(GenerateSqlPart<T, K> closure);

    /**
     * not exists一个闭包
     * @param sql 闭包
     * @return 查询构建器
     */
    Builder<T, K> whereNotExistsRaw(String sql);

    /**
     * not exists一个完整sql
     * @param closure 完整sql
     * @return 查询构建器
     */
    Builder<T, K> whereNotExists(GenerateSqlPart<T, K> closure);

    /**
     * 比较字段与字段
     * @param column1 列1
     * @param symbol  比较关系
     * @param column2 列2
     * @return 查询构建器
     */
    Builder<T, K> whereColumn(String column1, String symbol, String column2);

    /**
     * 字段与字段相等
     * @param column1 列1
     * @param column2 列2
     * @return 查询构建器
     */
    Builder<T, K> whereColumn(String column1, String column2);

    /**
     * 且
     * @param closure 闭包
     * @return 查询构建器
     */
    Builder<T, K> andWhere(GenerateSqlPart<T, K> closure);

    /**
     * 或
     * @param closure 闭包
     * @return 查询构建器
     */
    Builder<T, K> orWhere(GenerateSqlPart<T, K> closure);

}
