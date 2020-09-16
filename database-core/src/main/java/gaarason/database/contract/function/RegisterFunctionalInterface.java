package gaarason.database.contract.function;

import gaarason.database.contract.eloquent.Model;

/**
 * @param <T> 实体类
 * @param <K> 主键类
 */
@FunctionalInterface
public interface RegisterFunctionalInterface<T, K> {

    /**
     * 执行并获取模型对象
     * @param modelClass 模型类
     * @return 模型对象
     */
    Model<T, K> execute(Class<? extends Model<T, K>> modelClass);

}
