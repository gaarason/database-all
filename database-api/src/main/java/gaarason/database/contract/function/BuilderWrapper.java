package gaarason.database.contract.function;

import gaarason.database.contract.eloquent.Builder;

import java.io.Serializable;

/**
 * 查询构造器包装
 * 生成代码片段
 * @param <T> 实体类型
 * @param <K> 主键类型
 * @author xt
 */
@FunctionalInterface
public interface BuilderWrapper<B extends Builder<B, T, K>, T, K> extends Serializable {

    /**
     * 通用空实现
     */
    @SuppressWarnings("rawtypes")
    BuilderWrapper EMPTY = builder -> builder;

    /**
     * 生成代码片段
     * @param builder 生成器
     * @return 生成器
     */
    Builder<B, T, K> execute(Builder<B, T, K> builder);

    /**
     * 通用空实现
     * @param <TT> 指定实体类型
     * @param <KK> 指定主键类型
     * @return 生成代码片段
     */
    @SuppressWarnings("unchecked")
    static <BB extends Builder<BB, TT, KK>, TT, KK> BuilderWrapper<BB, TT, KK> empty() {
        return (BuilderWrapper<BB, TT, KK>) EMPTY;
    }
}
