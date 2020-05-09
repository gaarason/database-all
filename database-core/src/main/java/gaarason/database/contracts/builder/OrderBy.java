package gaarason.database.contracts.builder;

import gaarason.database.query.Builder;

/**
 * 排序
 * @param <T>
 * @param <K>
 */
public interface OrderBy<T, K> {

    /**
     * 排序
     * @param column 列名
     * @param orderByType 正序|倒序
     * @return 查询构造器
     */
    Builder<T, K> orderBy(String column, gaarason.database.eloquent.enums.OrderBy orderByType);

    /**
     * 排序
     * @param column 列名
     * @return 查询构造器
     */
    Builder<T, K> orderBy(String column);

}
