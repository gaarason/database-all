package gaarason.database.provider;

import gaarason.database.contract.function.InstanceCreatorFunctionalInterface;
import gaarason.database.core.Container;
import gaarason.database.exception.InvalidConfigException;
import gaarason.database.exception.ObjectNewInstanceException;
import gaarason.database.logging.Log;
import gaarason.database.logging.LogFactory;
import gaarason.database.util.ClassUtils;
import gaarason.database.util.ObjectUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 容器
 * @author xt
 */
public class ContainerProvider implements Container {

    private static final Log LOGGER = LogFactory.getLog(ContainerProvider.class);

    /**
     * 实例化工厂 MAP
     */
    protected final ConcurrentHashMap<Class<?>, LinkedList<InstanceCreatorFunctionalInterface<?>>> INSTANCE_CREATOR_MAP = new ConcurrentHashMap<>();

    /**
     * 实例对象 MAP
     */
    protected final ConcurrentHashMap<Class<?>, LinkedList<Object>> INSTANCE_MAP = new ConcurrentHashMap<>();


    /**
     * 注册 实例化工厂
     * 只要没有实例化, 那么可以重复注册, 且后注册的优先级更高
     * @param closure 实例化工厂
     */
    @Override
    public synchronized <T> void register(Class<T> interfaceClass, InstanceCreatorFunctionalInterface<T> closure) {
        if (INSTANCE_MAP.get(interfaceClass) != null) {
            throw new InvalidConfigException(interfaceClass + " should be registered before get bean.");
        }
        // 添加到头部
        LinkedList<InstanceCreatorFunctionalInterface<?>> instanceCreators = INSTANCE_CREATOR_MAP.computeIfAbsent(interfaceClass, k -> new LinkedList<>());
        instanceCreators.push(closure);
        instanceCreators.sort(Comparator.comparing(InstanceCreatorFunctionalInterface::getOrder));
    }

    /**
     * 返回一个对象列表, 其中的每个对象必然单例
     * @param interfaceClass 接口类型
     * @return 对象列表
     */
    @Override
    public <T> List<T> getBeans(Class<T> interfaceClass) {
        return getBeansInside(interfaceClass, false);
    }

    /**
     * 返回一个对象, 必然单例
     * @param interfaceClass 接口类型
     * @return 对象
     */
    @Override
    public <T> T getBean(Class<T> interfaceClass) {
        return getBeansInside(interfaceClass, true).get(0);
    }

    /**
     * 返回一个对象列表, 其中的每个对象必然单例
     * @param interfaceClass 接口类型
     * @param fastReturn     获取一个实例对象, 就快速返回
     * @param <T>            类型
     * @return 对象
     */
    protected <T> List<T> getBeansInside(Class<T> interfaceClass, boolean fastReturn) {
        LinkedList<Object> objects = INSTANCE_MAP.getOrDefault(interfaceClass, new LinkedList<>());
        List<InstanceCreatorFunctionalInterface<?>> instanceCreators = INSTANCE_CREATOR_MAP.getOrDefault(interfaceClass, new LinkedList<>());
        /*
         * 对象列表没有值 || 在非快速返回的情况下, 对象列表没有足够的对象
         * 以上的2种情况, 都需要进行实例化 ( 不重复的进行实例化 )
         */
        if (objects.isEmpty() || (!fastReturn && objects.size() < instanceCreators.size())) {
            synchronized (interfaceClass.getName()) {
                objects = INSTANCE_MAP.getOrDefault(interfaceClass, new LinkedList<>());
                instanceCreators = INSTANCE_CREATOR_MAP.getOrDefault(interfaceClass, new LinkedList<>());
                // 串行检查
                if (objects.isEmpty() || (!fastReturn && objects.size() < instanceCreators.size())) {
                    /*
                     * 优先 使用注册的方式
                     * 保底 使用默认的方式
                     * 仅仅改变局部变量
                     */
                    if (instanceCreators.isEmpty()) {
                        instanceCreators.add(defaultNewInstance(interfaceClass));
                    }

                    try {
                        int numberOfObjectsAlreadyExist = objects.size();
                        for (InstanceCreatorFunctionalInterface<?> instanceCreator : instanceCreators) {
                            // 略过已经实例化过的对象.
                            if (numberOfObjectsAlreadyExist-- > 0) {
                                continue;
                            }
                            objects.add(instanceCreator.execute(ObjectUtils.typeCast(interfaceClass)));
                            if (fastReturn) {
                                break;
                            }
                        }

                        INSTANCE_MAP.put(interfaceClass, objects);
                    } catch (Throwable e) {
                        throw new ObjectNewInstanceException(interfaceClass, e.getMessage(), e);
                    }
                }
            }
        }
        return ObjectUtils.typeCast(objects);
    }

    /**
     * 缺省实例化方式
     * @param clazz 类
     * @param <T>   类型
     */
    protected  <T> InstanceCreatorFunctionalInterface<T> defaultNewInstance(Class<T> clazz) {
        return c -> {
            LOGGER.info("Instantiate unregistered objects[" + clazz.getName() + "] by default.");
            return ClassUtils.newInstance(clazz);
        };
    }

}
