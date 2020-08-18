package gaarason.database.contracts.function;

import gaarason.database.query.Builder;

@FunctionalInterface
public interface GenerateSqlPart {
    
    /**
     * 生成代码片段
     * @param builder 生成器
     * @return 生成器
     */
    Builder<?, ?> generate(Builder<?, ?> builder);
}
