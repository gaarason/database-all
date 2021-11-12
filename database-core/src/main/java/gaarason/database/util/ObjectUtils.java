package gaarason.database.util;

import gaarason.database.core.lang.Nullable;
import gaarason.database.exception.AbnormalParameterException;
import gaarason.database.exception.CloneNotSupportedRuntimeException;
import gaarason.database.exception.TypeCastException;
import gaarason.database.exception.TypeNotSupportedException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 对象工具类
 * @author xt
 */
public class ObjectUtils {

    /**
     * 线程安全随机对象
     */
    private static final ThreadLocalRandom random = ThreadLocalRandom.current();

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
     * 逻辑类型转换
     * @param original 原始对象
     * @param clz      目标类
     * @param <T>      原始类型
     * @param <N>      目标类型
     * @return 目标对象
     * @throws TypeCastException 类型转化失败
     */
    public static <T, N> N typeCast(T original, final Class<N> clz) throws TypeCastException {
        return ConverterUtils.cast(original, clz);
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

    /**
     * 判断是否为空
     * @param array 数组
     * @return bool
     */
    public static boolean isEmpty(@Nullable Object[] array) {
        return array == null || array.length == 0;
    }

    /**
     * 判断是否为空
     * @param obj 所有类型
     * @return bool
     */
    public static boolean isEmpty(@Nullable Object obj) {
        if (obj == null) {
            return true;
        } else if (obj instanceof Optional) {
            return !((Optional<?>) obj).isPresent();
        } else if (obj instanceof CharSequence) {
            return ((CharSequence) obj).length() == 0;
        } else if (obj.getClass().isArray()) {
            return Array.getLength(obj) == 0;
        } else if (obj instanceof Collection) {
            return ((Collection<?>) obj).isEmpty();
        } else {
            return obj instanceof Map && ((Map<?, ?>) obj).isEmpty();
        }
    }

    /**
     * 判断是否为null
     * @param obj 所有类型
     * @return bool
     */
    public static boolean isNull(@Nullable Object obj) {
        if (obj == null) {
            return true;
        } else if (obj instanceof Optional) {
            return !((Optional<?>) obj).isPresent();
        }
        return false;
    }

    /**
     * Determine if the given objects are equal, returning {@code true} if
     * both are {@code null} or {@code false} if only one is {@code null}.
     * <p>Compares arrays with {@code Arrays.equals}, performing an equality
     * check based on the array elements rather than the array reference.
     * @param o1 first Object to compare
     * @param o2 second Object to compare
     * @return whether the given objects are equal
     * @see Object#equals(Object)
     * @see java.util.Arrays#equals
     */
    public static boolean nullSafeEquals(@Nullable Object o1, @Nullable Object o2) {
        if (o1 == o2) {
            return true;
        }
        if (o1 == null || o2 == null) {
            return false;
        }
        if (o1.equals(o2)) {
            return true;
        }
        if (o1.getClass().isArray() && o2.getClass().isArray()) {
            return arrayEquals(o1, o2);
        }
        return false;
    }

    /**
     * Compare the given arrays with {@code Arrays.equals}, performing an equality
     * check based on the array elements rather than the array reference.
     * @param o1 first array to compare
     * @param o2 second array to compare
     * @return whether the given objects are equal
     * @see #nullSafeEquals(Object, Object)
     * @see java.util.Arrays#equals
     */
    private static boolean arrayEquals(Object o1, Object o2) {
        if (o1 instanceof Object[] && o2 instanceof Object[]) {
            return Arrays.equals((Object[]) o1, (Object[]) o2);
        }
        if (o1 instanceof boolean[] && o2 instanceof boolean[]) {
            return Arrays.equals((boolean[]) o1, (boolean[]) o2);
        }
        if (o1 instanceof byte[] && o2 instanceof byte[]) {
            return Arrays.equals((byte[]) o1, (byte[]) o2);
        }
        if (o1 instanceof char[] && o2 instanceof char[]) {
            return Arrays.equals((char[]) o1, (char[]) o2);
        }
        if (o1 instanceof double[] && o2 instanceof double[]) {
            return Arrays.equals((double[]) o1, (double[]) o2);
        }
        if (o1 instanceof float[] && o2 instanceof float[]) {
            return Arrays.equals((float[]) o1, (float[]) o2);
        }
        if (o1 instanceof int[] && o2 instanceof int[]) {
            return Arrays.equals((int[]) o1, (int[]) o2);
        }
        if (o1 instanceof long[] && o2 instanceof long[]) {
            return Arrays.equals((long[]) o1, (long[]) o2);
        }
        if (o1 instanceof short[] && o2 instanceof short[]) {
            return Arrays.equals((short[]) o1, (short[]) o2);
        }
        return false;
    }

    /**
     * 返回count个不重复的 0到bound 之间的随机数
     * @param bound 随机数上限(不包含) 说明:当bound=2时,随机数只会出现 0,1
     * @param count 返回随机数的数量
     * @return 不重复的随机数
     */
    public static Set<Integer> random(int bound, int count) {
        // 获取半数以上的随机数, 则反向获取
        if (bound / 2 < count) {
            Set<Integer> res = new HashSet<>(count);
            final Set<Integer> integers = randomReal(bound, bound - count);
            for (int i = 0; i < bound; i++) {
                if (!integers.contains(i)) {
                    res.add(i);
                }
            }
            return res;
        } else {
            return randomReal(bound, count);
        }
    }

    /**
     * 返回count个不重复的 0到bound 之间的随机数
     * @param bound 随机数上限(不包含) 说明:当bound=2时,随机数只会出现 0,1
     * @param count 返回随机数的数量
     * @return 不重复的随机数
     */
    protected static Set<Integer> randomReal(int bound, int count) {
        if (bound < count) {
            throw new AbnormalParameterException();
        }
        Set<Integer> res = new HashSet<>(count);
        while (res.size() < count) {
            res.add(random.nextInt(bound));
        }
        return res;
    }

}
