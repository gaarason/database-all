package gaarason.database.contract.builder;

import gaarason.database.contract.eloquent.Builder;

/**
 * 指定索引
 * @param <T>
 * @param <K>
 * @author xt
 */
public interface Index<B extends Builder<B, T, K>, T, K> {

    /**
     * 指定使用索引
     * @param indexName 索引名称
     * @return 查询构造器
     */
    B forceIndex(String indexName);

    /**
     * 指定忽略索引
     * @param indexName 索引名称
     * @return 查询构造器
     */
    B ignoreIndex(String indexName);
}
