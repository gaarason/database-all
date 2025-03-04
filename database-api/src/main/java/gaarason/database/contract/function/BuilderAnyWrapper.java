package gaarason.database.contract.function;

import gaarason.database.contract.eloquent.Builder;
import gaarason.database.exception.TypeCastException;

import java.io.Serializable;

/**
 * 查询构造器包装
 * 生成代码片段
 * @author xt
 */
@FunctionalInterface
public interface BuilderAnyWrapper extends Serializable {

    /**
     * 通用空实现
     */
    BuilderAnyWrapper EMPTY = builder -> builder;

    /**
     * 生成代码片段
     * @param builder 生成器
     * @return 生成器
     */
    Builder<?, ?, ?> execute(Builder<?, ?, ?> builder);

    /**
     * 通用空实现
     * @return 生成代码片段
     */
    static BuilderAnyWrapper empty() {
        return EMPTY;
    }

    static BuilderAnyWrapper turn2(BuilderWrapper<?, ?, ?> builderWrapper) {
        return builder -> builderWrapper.execute(typeCast(builder));
    }

    @SuppressWarnings("unchecked")
    static <T, N> N typeCast(T original) throws TypeCastException {
        try {
            return (N) original;
        } catch (Exception e) {
            throw new TypeCastException(e.getMessage(), e);
        }
    }
}
