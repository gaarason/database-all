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
public interface BuilderWrapper<T, K> extends Serializable {

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
    Builder<T, K> execute(Builder<T, K> builder);

    /**
     * 通用空实现
     * @param <TT> 指定实体类型
     * @param <KK> 指定主键类型
     * @return 生成代码片段
     */
    @SuppressWarnings("unchecked")
    static <TT, KK> BuilderWrapper<TT, KK> empty() {
        return (BuilderWrapper<TT, KK>) EMPTY;
    }
}
