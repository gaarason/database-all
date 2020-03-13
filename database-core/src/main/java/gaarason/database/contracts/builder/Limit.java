package gaarason.database.contracts.builder;

import gaarason.database.query.Builder;

/**
 * 限制
 * @param <T>
 */
public interface Limit<T> {

    /**
     * 查询数量限制
     * @param offset 偏移量
     * @param take   获取数量
     * @return 查询构造器
     */
    Builder<T> limit(int offset, int take);

    /**
     * 查询数量限制
     * @param take 获取数量
     * @return 查询构造器
     */
    Builder<T> limit(int take);


}
