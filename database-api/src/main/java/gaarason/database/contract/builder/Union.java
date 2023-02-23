package gaarason.database.contract.builder;

import gaarason.database.contract.eloquent.Builder;
import gaarason.database.contract.function.BuilderWrapper;

/**
 * 结果集连接
 * @param <T>
 * @param <K>
 * @author xt
 */
public interface Union<T, K> {

    /**
     * 结果集连接(去重)
     * @param closure 返回代码片段
     * @return 查询构造器
     */
    Builder<T, K> union(BuilderWrapper<T, K> closure);

    /**
     * 结果集连接(不去重)
     * @param closure 返回代码片段
     * @return 查询构造器
     */
    Builder<T, K> unionAll(BuilderWrapper<T, K> closure);
}
