package gaarason.database.contracts.builder;

import gaarason.database.query.Builder;

import java.util.List;

/**
 * 分组
 * @param <T>
 */
public interface Group<T> {

    /**
     * 分组
     * @param sqlPart 原生语句
     * @return 查询构造器
     */
    Builder<T> groupRaw(String sqlPart);

    /**
     * 分组
     * @param column 列名
     * @return 查询构造器
     */
    Builder<T> group(String column);

    /**
     * 分组
     * @param column 列名数组
     * @return 查询构造器
     */
    Builder<T> group(String... column);

    /**
     * 分组
     * @param columnList 列名列表
     * @return 查询构造器
     */
    Builder<T> group(List<String> columnList);

}
