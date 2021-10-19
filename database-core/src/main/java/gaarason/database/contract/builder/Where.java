package gaarason.database.contract.builder;

import gaarason.database.contract.eloquent.Builder;
import gaarason.database.contract.function.GenerateSqlPartFunctionalInterface;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

/**
 * 条件
 * @param <T>
 * @param <K>
 * @author xt
 */
public interface Where<T extends Serializable, K extends Serializable> {

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
    Builder<T, K> where(String column, String symbol, Object value);

    /**
     * 比较列与值相等
     * @param column 列名
     * @param value  值
     * @return 查询构建器
     */
    Builder<T, K> where(String column, Object value);

    /**
     * 将对象的属性转化为, 列与值相等的查询条件
     * @param entity 实体对象
     * @return 查询构建器
     */
    Builder<T, K> where(T entity);

    /**
     * 列与值相等的查询条件
     * @param map 条件map
     * @return 查询构建器
     */
    Builder<T, K> where(Map<String, Object> map);

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
    Builder<T, K> whereSubQuery(String column, String symbol, GenerateSqlPartFunctionalInterface<T, K> closure);

    /**
     * 列值在范围内
     * @param column    列名
     * @param valueList 值所在的list
     * @return 查询构建器
     */
    Builder<T, K> whereIn(String column, Collection<?> valueList);

    /**
     * 列值在范围内
     * @param column     列名
     * @param valueArray 值所在的数组
     * @return 查询构建器
     */
    Builder<T, K> whereIn(String column, Object... valueArray);

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
    Builder<T, K> whereIn(String column, GenerateSqlPartFunctionalInterface<T, K> closure);

    /**
     * 列值不在范围内
     * @param column    列名
     * @param valueList 值所在的list
     * @return 查询构建器
     */
    Builder<T, K> whereNotIn(String column, Collection<?> valueList);

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
    Builder<T, K> whereNotIn(String column, GenerateSqlPartFunctionalInterface<T, K> closure);

    /**
     * 列值在范围内
     * @param column     列名
     * @param valueArray 值所在的数组
     * @return 查询构建器
     */
    Builder<T, K> whereNotIn(String column, Object... valueArray);

    /**
     * 列值在2值之间
     * @param column 列名
     * @param min    值1
     * @param max    值2
     * @return 查询构建器
     */
    Builder<T, K> whereBetween(String column, Object min, Object max);

    /**
     * 列值不在2值之间
     * @param column 列名
     * @param min    值1
     * @param max    值2
     * @return 查询构建器
     */
    Builder<T, K> whereNotBetween(String column, Object min, Object max);

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
    Builder<T, K> whereExists(GenerateSqlPartFunctionalInterface<T, K> closure);

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
    Builder<T, K> whereNotExists(GenerateSqlPartFunctionalInterface<T, K> closure);

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
    Builder<T, K> andWhere(GenerateSqlPartFunctionalInterface<T, K> closure);

    /**
     * 或
     * @param closure 闭包
     * @return 查询构建器
     */
    Builder<T, K> orWhere(GenerateSqlPartFunctionalInterface<T, K> closure);

}
