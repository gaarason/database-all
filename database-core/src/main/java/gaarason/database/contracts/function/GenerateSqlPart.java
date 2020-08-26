package gaarason.database.contracts.function;

import gaarason.database.query.Builder;

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
