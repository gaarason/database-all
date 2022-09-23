package gaarason.database.util;

import gaarason.database.appointment.LambdaInfo;
import gaarason.database.contract.function.ColumnFunctionalInterface;
import gaarason.database.exception.LambdaColumnException;
import gaarason.database.lang.Nullable;
import gaarason.database.logging.Log;
import gaarason.database.logging.LogFactory;
import gaarason.database.provider.ModelShadowProvider;

import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Locale;

/**
 * 解析 Lambda
 */
public class LambdaUtils {

    private static final Log LOGGER = LogFactory.getLog(LambdaUtils.class);

    @Nullable
    private static final Field FIELD_CAPTURING_CLASS;

    static {
        Field localField;
        try {
            Class<SerializedLambda> lambdaClass = SerializedLambda.class;
            localField = lambdaClass.getDeclaredField("capturingClass");
            localField.setAccessible(true);
        } catch (Throwable e) {
            LOGGER.warn(e.getMessage());
            localField = null;
        }
        FIELD_CAPTURING_CLASS = localField;
    }

    /**
     * 将lambda风格的属性名, 解析为String类型
     * 比较慢, 实际场景建议缓存使用
     * @param func lambda风格的属性名
     * @param <T> 实体类型
     * @return 属性名
     * @see ModelShadowProvider
     */
    public static <T> LambdaInfo<T> parse(ColumnFunctionalInterface<T> func) {
        try {
            Method method = func.getClass().getDeclaredMethod("writeReplace");
            method.setAccessible(true);
            SerializedLambda lambda = (SerializedLambda) method.invoke(func);
            // 类
            Class<?> instantiatedMethodTypeClass = getInstantiatedMethodTypeClass(lambda);
            // 属性名 (目前还是方法名)
            String propertyName = lambda.getImplMethodName();
            if (propertyName.startsWith("is")) {
                propertyName = propertyName.substring(2);
            } else if (propertyName.startsWith("get") || propertyName.startsWith("set")) {
                propertyName = propertyName.substring(3);
            }
            if (propertyName.length() == 1 ||
                (propertyName.length() > 1 && !Character.isUpperCase(propertyName.charAt(1)))) {
                propertyName = propertyName.substring(0, 1).toLowerCase(Locale.ENGLISH) + propertyName.substring(1);
            }

            String columnName = EntityUtils.columnName(
                EntityUtils.getDeclaredFieldContainParent(instantiatedMethodTypeClass, propertyName));

            return new LambdaInfo<>(propertyName, columnName, ObjectUtils.typeCast(instantiatedMethodTypeClass));
        } catch (Throwable e) {
            throw new LambdaColumnException(e);
        }
    }

    /**
     * 获取lambda的实现的类
     * @param lambda 表达式 eg: Stu::getName
     * @return 实现的类 eg: Stu
     * @throws IllegalAccessException 权限异常
     */
    private static Class<?> getInstantiatedMethodTypeClass(SerializedLambda lambda) throws IllegalAccessException {
        String instantiatedMethodType = lambda.getInstantiatedMethodType();
        String pathName = instantiatedMethodType.substring(2, instantiatedMethodType.indexOf(';'));
        String className = StringUtils.replace(pathName, "/", ".");
        ClassLoader classLoader =
            FIELD_CAPTURING_CLASS != null ? ((Class<?>) FIELD_CAPTURING_CLASS.get(lambda)).getClassLoader() : null;
        return ClassUtils.forName(className, classLoader);
    }
}
