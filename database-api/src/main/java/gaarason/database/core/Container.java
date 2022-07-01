package gaarason.database.core;

import gaarason.database.contract.function.InstanceCreatorFunctionalInterface;

import java.util.List;

public interface Container {

    /**
     * 注册 实例化工厂
     * 只要没有实例化, 那么可以重复注册, 且后注册的优先级更高
     * @param closure 实例化工厂
     */
    <T> void register(Class<T> interfaceClass, InstanceCreatorFunctionalInterface<T> closure);

    /**
     * 返回一个对象列表, 其中的每个对象必然单例
     * @param interfaceClass 接口类型
     * @return 对象列表
     */
    <T> List<T> getBeans(Class<T> interfaceClass);

    /**
     * 返回一个对象, 必然单例
     * @param interfaceClass 接口类型
     * @return 对象
     */
    <T> T getBean(Class<T> interfaceClass);


    /**
     * 用于持有容器
     */
    interface Keeper {
        /**
         * 获取容器
         * @return 容器
         */
        Container getContainer();
    }

    abstract class SimpleKeeper implements Keeper {

        protected final Container container;

        public SimpleKeeper(Container container) {
            this.container = container;
        }
        @Override
        public Container getContainer() {
            return container;
        }
    }

}
