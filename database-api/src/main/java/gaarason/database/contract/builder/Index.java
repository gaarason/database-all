package gaarason.database.contract.builder;

import gaarason.database.contract.eloquent.Builder;

import java.io.Serializable;

/**
 * 指定索引
 * @param <T>
 * @param <K>
 * @author xt
 */
public interface Index<T extends Serializable, K extends Serializable> {

    /**
     * 指定使用索引
     * @param indexName 索引名称
     * @return 查询构造器
     */
    Builder<T, K> forceIndex(String indexName);

    /**
     * 指定忽略索引
     * @param indexName 索引名称
     * @return 查询构造器
     */
    Builder<T, K> ignoreIndex(String indexName);
}
