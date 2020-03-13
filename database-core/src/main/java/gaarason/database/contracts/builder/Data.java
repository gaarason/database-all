package gaarason.database.contracts.builder;

import gaarason.database.query.Builder;

import java.util.Map;

/**
 * 数据
 * @param <T>
 */
public interface Data<T> {

    /**
     * 数据更新
     * @param sqlPart sql片段 eg: age=15,name="dd"
     * @return 查询构建器
     */
    Builder<T> data(String sqlPart);

    /**
     * 数据更新
     * @param column 列名
     * @param value  值
     * @return 查询构建器
     */
    Builder<T> data(String column, String value);

    /**
     * 数据更新
     * @param map Map<String column, String value>
     * @return 查询构建器
     */
    Builder<T> data(Map<String, String> map);

    /**
     * 字段自增
     * @param column 列名
     * @param steps  步长
     * @return 查询构建器
     */
    Builder<T> dataIncrement(String column, int steps);

    /**
     * 字段自减
     * @param column 列名
     * @param steps  步长
     * @return 查询构建器
     */
    Builder<T> dataDecrement(String column, int steps);

}
