package gaarason.database.contract.function;

/**
 * @param <T> 实体类
 */
@FunctionalInterface
public interface InstanceCreatorFunctionalInterface<T> {

    T execute(Class<T> clazz);

}
