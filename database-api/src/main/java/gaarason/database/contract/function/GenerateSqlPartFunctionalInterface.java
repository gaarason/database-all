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
     * 生成代码片段
     * @param builder 生成器
     * @return 生成器
     */
    Builder<T, K> execute(Builder<T, K> builder);
}
