package gaarason.database.util;

import gaarason.database.exception.CloneNotSupportedRuntimeException;
import gaarason.database.exception.TypeCastException;
import gaarason.database.exception.TypeNotSupportedException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * 对象工具类
 * @author xt
 */
public class ObjectUtils {

    private ObjectUtils() {
    }


    /**
     * 获取指定类中的第index个泛型的类
     * @param clazz 指定类
     * @param index 第几个
     * @param <A>   泛型的类
     * @param <B>   指定类型
     * @return 泛型的类
     */
    @SuppressWarnings("unchecked")
    public static <A, B> Class<A> getGenerics(Class<B> clazz, int index) {
        return (Class<A>) ((ParameterizedType) clazz.getGenericSuperclass())
            .getActualTypeArguments()[index];
    }

    /**
     * 获取指定类中的第index个泛型的类
     * @param parameterizedType 指定类
     * @param index             第几个
     * @param <A>               泛型的类
     * @return 泛型的类
     */
    @SuppressWarnings("unchecked")
    public static <A> Class<A> getGenerics(ParameterizedType parameterizedType, int index) {
        return (Class<A>) parameterizedType.getActualTypeArguments()[index];
    }

    /**
     * 是否是集合类型
     * @param clazz 类型
     * @return 是否
     */
    public static boolean isCollection(Class<?> clazz) {
        return Arrays.asList(clazz.getInterfaces()).contains(Collection.class);
    }


    /**
     * 通过序列化对普通对象进行递归copy
     * @param original 源对象
     * @param <T>      对象所属的类
     * @return 全新的对象
     * @throws CloneNotSupportedRuntimeException 克隆异常
     */
    @SuppressWarnings("unchecked")
    public static <T> T deepCopy(T original) {
        try {
            ByteArrayOutputStream bis = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bis);
            oos.writeObject(original);
            oos.flush();
            ObjectInputStream input = new ObjectInputStream(new ByteArrayInputStream(bis.toByteArray()));
            return (T) input.readObject();
        } catch (Exception e) {
            throw new CloneNotSupportedRuntimeException(e.getMessage(), e);
        }
    }

    /**
     * 强制类型转换
     * @param original 原始对象
     * @param <T>      原始类型
     * @param <N>      目标类型
     * @return 目标对象
     * @throws TypeCastException 类型转化失败
     */
    @SuppressWarnings("unchecked")
    public static <T, N> N typeCast(T original) throws TypeCastException {
        try {
            return (N) original;
        } catch (Exception e) {
            throw new TypeCastException(e.getMessage(), e);
        }
    }

    /**
     * 类型转换
     * @param original 原始对象
     * @param <T>      原始类型
     * @param <N>      目标类型
     * @return 目标对象
     */
    @SuppressWarnings("unchecked")
    public static <T, N> N typeCast(T original, Class<?> type) {
        try {


            return (N) original;
        } catch (Exception e) {
            throw new TypeCastException(e.getMessage(), e);
        }
    }

    /**
     * 属性是否在类中存在(多层级)
     * 集合类型的属性,将会使用第一个泛型类型
     * @param detectedClass     待检测的类
     * @param multipleAttribute 检测的属性 eg: teacher.student.id
     * @return 是否存在
     */
    public static boolean checkProperties(Class<?> detectedClass, String multipleAttribute) {
        String[] attrArr = multipleAttribute.split("\\.");
        try {
            Class<?> tempClass = detectedClass;
            for (String attr : attrArr) {
                Field field = EntityUtils.getDeclaredFieldContainParent(tempClass, attr);
                tempClass = field.getType();
                boolean contains =
                    new ArrayList<>(Arrays.asList(tempClass.getInterfaces())).contains(Collection.class);
                // 如果是集合类型, 那么使用泛型对象
                if (contains) {
                    Type genericType = field.getGenericType();
                    // 如果是泛型
                    if (genericType instanceof ParameterizedType) {
                        ParameterizedType pt = (ParameterizedType) genericType;
                        // 得到泛型里的第一个class类型对象
                        tempClass = (Class<?>) pt.getActualTypeArguments()[0];
                    }
                }
            }
            return true;
        } catch (NoSuchFieldException e) {
            return false;
        } catch (Exception e) {
            throw new TypeNotSupportedException(e.getMessage(), e);
        }
    }
}
