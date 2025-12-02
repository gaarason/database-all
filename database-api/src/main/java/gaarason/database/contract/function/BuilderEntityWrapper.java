package gaarason.database.contract.function;

import gaarason.database.contract.eloquent.Builder;

import java.io.Serializable;

/**
 * 查询构造器包装
 * 生成代码片段
 * @param <T> 实体类型
 * @author xt
 */
@FunctionalInterface
public interface BuilderEntityWrapper<T> extends Serializable {
    /**
     * 生成代码片段
     * @param builder 生成器
     * @return 生成器 (可以改变, 但不影响)
     */
    Builder<?, ?, ?> execute(Builder<?, T, ?> builder);
}
