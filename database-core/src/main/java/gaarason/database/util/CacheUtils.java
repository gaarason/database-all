package gaarason.database.util;

import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Supplier;

public class CacheUtils {

    private static final ConcurrentMap<String, Object> CACHE = new ConcurrentHashMap<>();

    public static <T> T get(Supplier<T> func, Class<?> clazz, String methodName, Object... args) {
        String key = clazz + methodName + Arrays.toString(args);
        Object result = CACHE.computeIfAbsent(key, k -> func.get());
        return ObjectUtils.typeCastNullable(result);
    }
}
