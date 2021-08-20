package gaarason.database.contract.builder;

import gaarason.database.contract.eloquent.Builder;

import java.io.Serializable;

/**
 * 限制
 * @param <T>
 * @param <K>
 * @author xt
 */
public interface Limit<T extends Serializable, K extends Serializable> {

    /**
     * 查询数量限制
     * @param offset 偏移量
     * @param take   获取数量
     * @return 查询构造器
     */
    Builder<T, K> limit(int offset, int take);

    /**
     * 查询数量限制
     * @param take 获取数量
     * @return 查询构造器
     */
    Builder<T, K> limit(int take);


}
