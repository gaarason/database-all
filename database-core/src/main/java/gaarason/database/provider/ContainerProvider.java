package gaarason.database.provider;

import gaarason.database.contract.function.InstanceCreatorFunctionalInterface;
import gaarason.database.contract.support.IdGenerator;
import gaarason.database.exception.InvalidConfigException;
import gaarason.database.exception.ModelNewInstanceException;
import gaarason.database.support.SnowFlakeIdGenerator;
import gaarason.database.util.ObjectUtil;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 容器化对象实例
 */
final public class ContainerProvider {

    /**
     * 实例化工厂 MAP
     */
    private final static ConcurrentHashMap<Class<?>, InstanceCreatorFunctionalInterface<?>> instanceCreatorMap = new ConcurrentHashMap<>();

    /**
     * 实例对象 MAP
     */
    private final static ConcurrentHashMap<Class<?>, Object> instanceMap = new ConcurrentHashMap<>();

    /**
     * 初始化默认的
     */
    static {
        // ID生成 雪花算法
        instanceCreatorMap.put(IdGenerator.SnowFlakesID.class, (clazz -> new SnowFlakeIdGenerator(0, 0)));
        // ID生成 UUID 36
        instanceCreatorMap.put(IdGenerator.UUID36.class,
            (InstanceCreatorFunctionalInterface<IdGenerator.UUID36>) clazz ->
                () -> UUID.randomUUID().toString());
        // ID生成 UUID 32
        instanceCreatorMap.put(IdGenerator.UUID32.class,
            (InstanceCreatorFunctionalInterface<IdGenerator.UUID32>) clazz ->
                () -> UUID.randomUUID().toString().replace("-", ""));
        // ID生成 Never
        instanceCreatorMap.put(IdGenerator.Never.class,
            (InstanceCreatorFunctionalInterface<IdGenerator.Never>) clazz ->
                () -> null);
        // ID生成 自定义
        instanceCreatorMap.put(IdGenerator.Custom.class,
            (InstanceCreatorFunctionalInterface<IdGenerator.Custom>) clazz ->
                () -> null);
    }

    /**
     * 注册 实例化工厂
     * 只要没有实例化, 那么可以重复注册, 且后注册的覆盖先注册的
     * @param closure 实例化工厂
     */
    public static <T> void register(Class<T> interfaceClass, InstanceCreatorFunctionalInterface<T> closure) {
        if (instanceMap.get(interfaceClass) != null) {
            throw new InvalidConfigException("Should be registered before get bean.");
        }
        instanceCreatorMap.put(interfaceClass, closure);
    }


    /**
     * 返回一个对象, 必然单例
     * @return 对象
     */
    public static <T> T getBean(Class<T> interfaceClass) {
        if (instanceMap.get(interfaceClass) == null) {
            synchronized (interfaceClass) {
                if (instanceMap.get(interfaceClass) == null) {
                    InstanceCreatorFunctionalInterface<?> instanceCreatorFunctionalInterface = instanceCreatorMap.get(interfaceClass);
                    if (null == instanceCreatorFunctionalInterface) {
                        throw new InvalidConfigException("The interface[" + interfaceClass + "] has not been registered before get bean.");
                    }
                    try {
                        Object bean = instanceCreatorFunctionalInterface.execute(ObjectUtil.typeCast(interfaceClass));
                        instanceMap.put(interfaceClass, bean);
                    } catch (Throwable e) {
                        throw new ModelNewInstanceException(interfaceClass, e.getMessage(), e);
                    }
                }
            }
        }
        return ObjectUtil.typeCast(instanceMap.get(interfaceClass));
    }
}
