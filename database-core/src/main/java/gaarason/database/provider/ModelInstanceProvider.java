package gaarason.database.provider;

import gaarason.database.contract.eloquent.Builder;
import gaarason.database.contract.eloquent.Model;
import gaarason.database.contract.function.InstantiationModelFunctionalInterface;
import gaarason.database.core.Container;
import gaarason.database.exception.InvalidConfigException;
import gaarason.database.exception.ModelNewInstanceException;
import gaarason.database.util.ClassUtils;
import gaarason.database.util.ObjectUtils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Model的实例化的工厂的提供者
 * @author xt
 */
public class ModelInstanceProvider extends Container.SimpleKeeper {

    /**
     * Model实例化工厂 列表
     */
    protected final LinkedList<InstantiationModelFunctionalInterface<?, ?>> INSTANTIATIONS = new LinkedList<>();

    /**
     * 是否已经实例化过
     */
    protected volatile boolean executed;

    public ModelInstanceProvider(Container container) {
        super(container);
        init();
    }

    /**
     * 注册 Model实例化工厂
     * @param factory Model实例化工厂
     */
    public void register(InstantiationModelFunctionalInterface<?, ?> factory) {
        synchronized (this) {
            if (executed) {
                throw new InvalidConfigException("Should be registered before execution.");
            }
            INSTANTIATIONS.push(factory);
        }
    }

    /**
     * 返回一个模型(是否是单例, 仅取决于Model实例化工厂)
     * 当存在多个工厂时, 后加入的先执行, 只要执行正确则直接返回
     * @param modelClass 模型类
     * @param <T> 实体类
     * @param <K> 主键类
     * @return 模型对象
     * @throws ModelNewInstanceException 模型实例化失败
     */
    public <B extends Builder<B, T, K>, T, K> Model<B, T, K> getModel(
        Class<? extends Model<?, T, K>> modelClass) throws ModelNewInstanceException {
        synchronized (this) {
            executed = true;
            List<Throwable> throwableList = new ArrayList<>();
            for (InstantiationModelFunctionalInterface<?, ?> instantiation : INSTANTIATIONS) {
                try {
                    return ObjectUtils.typeCast(instantiation.execute(ObjectUtils.typeCast(modelClass)));
                } catch (Throwable e) {
                    throwableList.add(e);
                }
            }
            throw new ModelNewInstanceException(modelClass, throwableList);
        }
    }

    /**
     * 初始化 实例化方式
     */
    protected void init() {
        // 初始化默认的 Model实例化工厂
        INSTANTIATIONS.add(ClassUtils::newInstance);
    }
}
