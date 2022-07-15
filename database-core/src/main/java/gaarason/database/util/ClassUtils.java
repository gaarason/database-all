package gaarason.database.util;

import gaarason.database.exception.ClassNotFoundException;
import gaarason.database.exception.ObjectNewInstanceException;
import gaarason.database.lang.Nullable;
import gaarason.database.logging.Log;
import gaarason.database.logging.LogFactory;

import java.lang.reflect.Constructor;

/**
 * 类的相关操作
 */
public class ClassUtils {

    private static final Log LOGGER = LogFactory.getLog(ClassUtils.class);

    @Nullable
    private static final ClassLoader SYSTEM_CLASS_LOADER;

    static {
        ClassLoader localClassLoader;
        try {
            localClassLoader = ClassLoader.getSystemClassLoader();
        } catch (Throwable e) {
            LOGGER.warn(e.getMessage());
            localClassLoader = null;
        }
        SYSTEM_CLASS_LOADER = localClassLoader;
    }

    private ClassUtils() {}

    /**
     * 实例化对象
     * @param className 目标类名
     * @param <T> 目标类型
     * @return 实例
     */
    public static <T> T newInstance(String className) {
        return ObjectUtils.typeCast(newInstance(forName(className)));
    }

    /**
     * 实例化对象
     * @param clazz 目标类
     * @param <T> 目标类型
     * @return 实例
     */
    public static <T> T newInstance(Class<T> clazz) {
        try {
            Constructor<T> constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (Throwable e) {
            throw new ObjectNewInstanceException(clazz, e);
        }
    }


    /**
     * 类加载
     * @param className 类名
     * @return 类
     */
    public static Class<?> forName(String className) {
        return forName(className, null);
    }

    /**
     * 类加载
     * @param className 类名
     * @param classLoader 加载器
     * @return 类
     */
    public static Class<?> forName(String className, @Nullable ClassLoader classLoader) {
        return loadClass(className, getClassLoaders(classLoader));
    }

    /**
     * 类加载
     * @param className 类名
     * @param classLoaders 加载器
     * @return 类
     */
    private static Class<?> loadClass(String className, ClassLoader[] classLoaders) {
        for (ClassLoader classLoader : classLoaders) {
            if (classLoader != null) {
                try {
                    return Class.forName(className, true, classLoader);
                } catch (Throwable ignore) {

                }
            }
        }
        throw new ClassNotFoundException(className);
    }

    /**
     * 依次返回可用的classloader
     * @param classLoader 类加载器
     * @return 类加载器数组
     */
    private static ClassLoader[] getClassLoaders(@Nullable ClassLoader classLoader) {
        return new ClassLoader[]{classLoader, Thread.currentThread().getContextClassLoader(),
            ClassUtils.class.getClassLoader(), SYSTEM_CLASS_LOADER};
    }
}
