package gaarason.database.contract.builder;

import gaarason.database.contract.eloquent.Builder;

public interface Index<T, K> {

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
