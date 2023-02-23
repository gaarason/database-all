package gaarason.database.contract.builder;

import gaarason.database.contract.eloquent.Builder;
import gaarason.database.contract.function.BuilderWrapper;

/**
 * 条件子句
 * @param <T>
 * @param <K>
 * @author xt
 */
public interface When<T, K> {

    /**
     * 当condition==true时, 执行closure
     * @param condition 条件
     * @param closure 查询构造
     * @return 查询构造器
     */
    Builder<T, K> when(boolean condition, BuilderWrapper<T, K> closure);

    /**
     * 当condition==true时, 执行closureIfTrue, 反之执行closureIfFalse
     * @param condition 条件
     * @param closureIfTrue 查询构造
     * @param closureIfFalse 查询构造
     * @return 查询构造器
     */
    Builder<T, K> when(boolean condition, BuilderWrapper<T, K> closureIfTrue,
        BuilderWrapper<T, K> closureIfFalse);


}
