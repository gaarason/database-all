package gaarason.database.provider;

import gaarason.database.contract.eloquent.Model;
import gaarason.database.contract.function.RegisterFunctionalInterface;
import gaarason.database.exception.InvalidConfigException;
import gaarason.database.exception.ModelNewInstanceException;
import gaarason.database.util.ObjectUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Model的实例化
 */
final public class ModelInstanceProvider {

    private static volatile List<RegisterFunctionalInterface<?, ?>> registers = Collections.synchronizedList(
        new ArrayList<>());


    private static volatile boolean executed = false;

    static {
        registers.add((modelClass) -> {
            try {
                return modelClass.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                throw new ModelNewInstanceException(e.getMessage(), e);
            }
        });
    }

    public static void register(RegisterFunctionalInterface<?, ?> closure) {
        if (executed) {
            throw new InvalidConfigException("Should be registered before execution.");
        }
        registers.add(0, closure);
    }

    public static <T, K> Model<T, K> getModel(Class<? extends Model<T, K>> modelClass) {
        executed = true;
        for (RegisterFunctionalInterface<?, ?> register : registers) {
            try {
                return ObjectUtil.typeCast(register.execute(ObjectUtil.typeCast(modelClass)));
            } catch (Throwable ignored) {

            }
        }
        throw new ModelNewInstanceException();
    }
}
