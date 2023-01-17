package gaarason.database.contract.function;

import gaarason.database.contract.eloquent.Builder;

import java.io.Serializable;

/**
 * 生成代码片段
 * @author xt
 */
@FunctionalInterface
public interface GenerateSqlPartFunctionalInterface<T, K> extends Serializable {

    /**
     * 通用空实现
     */
    @SuppressWarnings("rawtypes")
    GenerateSqlPartFunctionalInterface EMPTY = builder -> builder;

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
    static <TT, KK> GenerateSqlPartFunctionalInterface<TT, KK> empty() {
        return (GenerateSqlPartFunctionalInterface<TT, KK>) EMPTY;
    }
}
