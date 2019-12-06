package gaarason.database.contracts.builder;

import gaarason.database.contracts.function.GenerateSqlPart;
import gaarason.database.query.Builder;

import java.util.List;

public interface Having<T> {

    /**
     * 加入sql片段
     * @param sqlPart sql片段
     * @return 查询构建器
     */
    Builder<T> havingRaw(String sqlPart);

    /**
     * 比较列与值
     * @param column 列名
     * @param symbol 比较关系
     * @param value  值
     * @return 查询构建器
     */
    Builder<T> having(String column, String symbol, String value);

    /**
     * 比较列与值相等
     * @param column 列名
     * @param value  值
     * @return 查询构建器
     */
    Builder<T> having(String column, String value);

    /**
     * 列值在范围内
     * @param column    列名
     * @param valueList 值所在的list
     * @return 查询构建器
     */
    Builder<T> havingIn(String column, List<Object> valueList);

    /**
     * 列值在范围内(子查询)
     * @param column  列名
     * @param sql 完整sql eg:select id from student having age>10
     * @return 查询构建器
     */
    Builder<T> havingInRaw(String column, String sql);

    /**
     * 列值在范围内(子查询)
     * @param column  列名
     * @param closure 闭包
     * @return 查询构建器
     */
    Builder<T> havingIn(String column, GenerateSqlPart<T> closure);

    /**
     * 列值不在范围内
     * @param column    列名
     * @param valueList 值所在的list
     * @return 查询构建器
     */
    Builder<T> havingNotIn(String column, List<Object> valueList);

    /**
     * 列值不在范围内(子查询)
     * @param column  列名
     * @param sql 完整sql eg:select id from student having age>10
     * @return 查询构建器
     */
    Builder<T> havingNotInRaw(String column, String sql);

    /**
     * 列值不在范围内(子查询)
     * @param column  列名
     * @param closure 闭包
     * @return 查询构建器
     */
    Builder<T> havingNotIn(String column, GenerateSqlPart<T> closure);

    /**
     * 列值在2值之间
     * @param column 列名
     * @param min    值1
     * @param max    值2
     * @return 查询构建器
     */
    Builder<T> havingBetween(String column, String min, String max);

    /**
     * 列值不在2值之间
     * @param column 列名
     * @param min    值1
     * @param max    值2
     * @return 查询构建器
     */
    Builder<T> havingNotBetween(String column, String min, String max);

    /**
     * 列值为null
     * @param column 列名
     * @return 查询构建器
     */
    Builder<T> havingNull(String column);

    /**
     * 列值不为null
     * @param column 列名
     * @return 查询构建器
     */
    Builder<T> havingNotNull(String column);

    /**
     * exists一个sql
     * @param sql 完整sql
     * @return 查询构建器
     */
    Builder<T> havingExistsRaw(String sql);

    /**
     * exists一个闭包
     * @param Closure 闭包
     * @return 查询构建器
     */
    Builder<T> havingExists(GenerateSqlPart<T> Closure);

    /**
     * not exists一个闭包
     * @param sql 闭包
     * @return 查询构建器
     */
    Builder<T> havingNotExistsRaw(String sql);

    /**
     * not exists一个完整sql
     * @param Closure 完整sql
     * @return 查询构建器
     */
    Builder<T> havingNotExists(GenerateSqlPart<T> Closure);

    /**
     * 比较字段与字段
     * @param column1 列1
     * @param symbol  比较关系
     * @param column2 列2
     * @return 查询构建器
     */
    Builder<T> havingColumn(String column1, String symbol, String column2);

    /**
     * 字段与字段相等
     * @param column1 列1
     * @param column2 列2
     * @return 查询构建器
     */
    Builder<T> havingColumn(String column1, String column2);

    /**
     * 且
     * @param closure 闭包
     * @return 查询构建器
     */
    Builder<T> andHaving(GenerateSqlPart<T> closure);

    /**
     * 或
     * @param closure 闭包
     * @return 查询构建器
     */
    Builder<T> orHaving(GenerateSqlPart<T> closure);
}
