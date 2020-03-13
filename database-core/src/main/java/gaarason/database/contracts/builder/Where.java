package gaarason.database.contracts.builder;

import gaarason.database.contracts.function.GenerateSqlPart;
import gaarason.database.query.Builder;

import java.util.List;

/**
 * 条件
 * @param <T>
 */
public interface Where<T> {

    /**
     * 加入sql片段
     * @param sqlPart sql片段
     * @return 查询构建器
     */
    Builder<T> whereRaw(String sqlPart);

    /**
     * 比较列与值
     * @param column 列名
     * @param symbol 比较关系
     * @param value  值
     * @return 查询构建器
     */
    Builder<T> where(String column, String symbol, String value);

    /**
     * 比较列与值相等
     * @param column 列名
     * @param value  值
     * @return 查询构建器
     */
    Builder<T> where(String column, String value);

    /**
     * 条件子查询
     * @param column      列名
     * @param symbol      关系符号
     * @param completeSql 完整sql
     * @return 查询构建器
     */
    Builder<T> whereSubQuery(String column, String symbol, String completeSql);

    /**
     * 条件子查询
     * @param column  列名
     * @param symbol  关系符号
     * @param closure 闭包
     * @return 查询构建器
     */
    Builder<T> whereSubQuery(String column, String symbol, GenerateSqlPart<T> closure);

    /**
     * 列值在范围内
     * @param column    列名
     * @param valueList 值所在的list
     * @return 查询构建器
     */
    Builder<T> whereIn(String column, List<Object> valueList);

    /**
     * 列值在范围内(子查询)
     * @param column 列名
     * @param sql    完整sql eg:select id from student where age>10
     * @return 查询构建器
     */
    Builder<T> whereInRaw(String column, String sql);

    /**
     * 列值在范围内(子查询)
     * @param column  列名
     * @param closure 闭包
     * @return 查询构建器
     */
    Builder<T> whereIn(String column, GenerateSqlPart<T> closure);

    /**
     * 列值不在范围内
     * @param column    列名
     * @param valueList 值所在的list
     * @return 查询构建器
     */
    Builder<T> whereNotIn(String column, List<Object> valueList);

    /**
     * 列值不在范围内(子查询)
     * @param column 列名
     * @param sql    完整sql eg:select id from student where age>10
     * @return 查询构建器
     */
    Builder<T> whereNotInRaw(String column, String sql);

    /**
     * 列值不在范围内(子查询)
     * @param column  列名
     * @param closure 闭包
     * @return 查询构建器
     */
    Builder<T> whereNotIn(String column, GenerateSqlPart<T> closure);

    /**
     * 列值在2值之间
     * @param column 列名
     * @param min    值1
     * @param max    值2
     * @return 查询构建器
     */
    Builder<T> whereBetween(String column, String min, String max);

    /**
     * 列值不在2值之间
     * @param column 列名
     * @param min    值1
     * @param max    值2
     * @return 查询构建器
     */
    Builder<T> whereNotBetween(String column, String min, String max);

    /**
     * 列值为null
     * @param column 列名
     * @return 查询构建器
     */
    Builder<T> whereNull(String column);

    /**
     * 列值不为null
     * @param column 列名
     * @return 查询构建器
     */
    Builder<T> whereNotNull(String column);

    /**
     * exists一个sql
     * @param sql 完整sql
     * @return 查询构建器
     */
    Builder<T> whereExistsRaw(String sql);

    /**
     * exists一个闭包
     * @param closure 闭包
     * @return 查询构建器
     */
    Builder<T> whereExists(GenerateSqlPart<T> closure);

    /**
     * not exists一个闭包
     * @param sql 闭包
     * @return 查询构建器
     */
    Builder<T> whereNotExistsRaw(String sql);

    /**
     * not exists一个完整sql
     * @param closure 完整sql
     * @return 查询构建器
     */
    Builder<T> whereNotExists(GenerateSqlPart<T> closure);

    /**
     * 比较字段与字段
     * @param column1 列1
     * @param symbol  比较关系
     * @param column2 列2
     * @return 查询构建器
     */
    Builder<T> whereColumn(String column1, String symbol, String column2);

    /**
     * 字段与字段相等
     * @param column1 列1
     * @param column2 列2
     * @return 查询构建器
     */
    Builder<T> whereColumn(String column1, String column2);

    /**
     * 且
     * @param closure 闭包
     * @return 查询构建器
     */
    Builder<T> andWhere(GenerateSqlPart<T> closure);

    /**
     * 或
     * @param closure 闭包
     * @return 查询构建器
     */
    Builder<T> orWhere(GenerateSqlPart<T> closure);

}
