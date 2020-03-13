package gaarason.database.contracts.builder;

import gaarason.database.query.Builder;

/**
 * 排序
 * @param <T>
 */
public interface OrderBy<T> {

    /**
     * 排序
     * @param column 列名
     * @param orderByType 正序|倒序
     * @return 查询构造器
     */
    Builder<T> orderBy(String column, gaarason.database.eloquent.enums.OrderBy orderByType);

    /**
     * 排序
     * @param column 列名
     * @return 查询构造器
     */
    Builder<T> orderBy(String column);

}
