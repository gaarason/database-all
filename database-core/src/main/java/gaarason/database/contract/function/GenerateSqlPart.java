package gaarason.database.contract.function;

import gaarason.database.contract.eloquent.Builder;

import java.io.Serializable;

@FunctionalInterface
public interface GenerateSqlPart extends Serializable {
    
    /**
     * 生成代码片段
     * @param builder 生成器
     * @return 生成器
     */
    Builder<?, ?> generate(Builder<?, ?> builder);
}
