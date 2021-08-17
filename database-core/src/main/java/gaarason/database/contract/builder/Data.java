package gaarason.database.contract.builder;

import gaarason.database.contract.eloquent.Builder;

import java.util.Map;

/**
 * 数据
 * @param <T>
 * @param <K>
 * @author xt
 */
public interface Data<T, K> {

    /**
     * 数据更新
     * @param sqlPart sql片段 eg: age=15,name="dd"
     * @return 查询构建器
     */
    Builder<T, K> data(String sqlPart);

    /**
     * 数据更新
     * @param column 列名
     * @param value  值
     * @return 查询构建器
     */
    Builder<T, K> data(String column, String value);

    /**
     * 数据更新
     * @param map Map<String column, String value>
     * @return 查询构建器
     */
    Builder<T, K> data(Map<String, String> map);

    /**
     * 字段自增
     * @param column 列名
     * @param steps  步长
     * @return 查询构建器
     */
    Builder<T, K> dataIncrement(String column, int steps);

    /**
     * 字段自减
     * @param column 列名
     * @param steps  步长
     * @return 查询构建器
     */
    Builder<T, K> dataDecrement(String column, int steps);

}
