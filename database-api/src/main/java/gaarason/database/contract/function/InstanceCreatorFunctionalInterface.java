package gaarason.database.contract.function;

/**
 * 实例化工厂
 * @param <T> 类
 * @author xt
 */
@FunctionalInterface
public interface InstanceCreatorFunctionalInterface<T> {

    /**
     * 实例化工厂
     * @param clazz 类
     * @return 对象
     */
    T execute(Class<T> clazz) throws Throwable;

}
