package gaarason.database.contract.function;

import gaarason.database.contract.eloquent.Model;

import java.io.Serializable;

/**
 * 执行并获取模型对象
 * @param <T> 实体类
 * @param <K> 主键类
 * @author xt
 */
@FunctionalInterface
public interface InstantiationModelFunctionalInterface<T extends Serializable, K extends Serializable> {

    /**
     * 执行并获取模型对象
     * @param modelClass 模型类
     * @return 模型对象
     */
    Model<T, K> execute(Class<? extends Model<T, K>> modelClass);

}
