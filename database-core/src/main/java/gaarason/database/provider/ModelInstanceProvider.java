package gaarason.database.provider;

import gaarason.database.contract.eloquent.Model;
import gaarason.database.contract.function.InstantiationModelFunctionalInterface;
import gaarason.database.exception.InvalidConfigException;
import gaarason.database.exception.ModelNewInstanceException;
import gaarason.database.util.ObjectUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Model的实例化
 */
final public class ModelInstanceProvider {

    /**
     * Model实例化工厂 列表
     */
    private final static List<InstantiationModelFunctionalInterface<?, ?>> instantiations = Collections.synchronizedList(
        new ArrayList<>());

    /**
     * 是否已经实例化过
     */
    private static volatile boolean executed = false;

    /**
     * 初始化默认的 Model实例化工厂
     */
    static {
        instantiations.add((modelClass) -> {
            try {
                return modelClass.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                throw new ModelNewInstanceException(modelClass, e.getMessage(), e);
            }
        });
    }

    /**
     * 注册 Model实例化工厂
     * @param closure Model实例化工厂
     */
    public static void register(InstantiationModelFunctionalInterface<?, ?> closure) {
        if (executed) {
            throw new InvalidConfigException("Should be registered before execution.");
        }
        instantiations.add(0, closure);
    }

    /**
     * 返回一个模型(是否是单例, 仅取决于Model实例化工厂)
     * 当存在多个工厂时, 后加入的先执行, 只要执行正确则直接返回
     * @param modelClass 模型类
     * @param <T> 实体类
     * @param <K> 主键类
     * @return 模型对象
     */
    public static <T, K> Model<T, K> getModel(Class<? extends Model<T, K>> modelClass) {
        executed = true;
        List<Throwable> throwableList = new ArrayList<>();
        for (InstantiationModelFunctionalInterface<?, ?> instantiation : instantiations) {
            try {
                return ObjectUtil.typeCast(instantiation.execute(ObjectUtil.typeCast(modelClass)));
            } catch (Throwable e) {
                throwableList.add(e);
            }
        }
        throw new ModelNewInstanceException(modelClass, throwableList);
    }
}
