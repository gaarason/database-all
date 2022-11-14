package gaarason.database.util;

import gaarason.database.lang.Nullable;
import gaarason.database.support.SoftCache;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * 枚举工具
 * @author xt
 */
public class EnumUtils {

    /**
     * int为索引的缓存
     */
    private static final SoftCache<Type, Enum<?>[]> INT_INDEX_CACHE = new SoftCache<>();

    /**
     * String为索引的缓存
     */
    private static final SoftCache<Type, Map<String, Enum<?>>> STR_INDEX_CACHE = new SoftCache<>();

    /**
     * int 空值
     */
    public static final int EMPTY_INT = -1;

    /**
     * String 空值
     */
    public static final String EMPTY_STR = "";

    private EnumUtils() {
    }

    /**
     * 枚举转int
     * @param anEnum 枚举对象
     * @return int
     */
    public static int toInt(@Nullable Enum<?> anEnum) {
        return ObjectUtils.isEmpty(anEnum) ? EMPTY_INT : anEnum.ordinal();
    }

    /**
     * 枚举转String
     * @param anEnum 枚举对象
     * @return String
     */
    public static String toStr(@Nullable Enum<?> anEnum) {
        return ObjectUtils.isEmpty(anEnum) ? EMPTY_STR : anEnum.name();
    }

    /**
     * int转枚举
     * @param originalValue 原始值
     * @param clazz 枚举类
     * @param <T> 枚举类型
     * @return 枚举对象
     */
    @Nullable
    public static <T extends Enum<T>> Enum<T> toEnum(@Nullable Integer originalValue, Class<T> clazz) {
        return toEnum(originalValue, (Type) clazz);
    }

    /**
     * int转枚举
     * @param originalValue 原始值
     * @param type 枚举类
     * @param <T> 枚举类型
     * @return 枚举对象
     */
    @Nullable
    public static <T extends Enum<T>> Enum<T> toEnum(@Nullable Integer originalValue, Type type) {
        if (originalValue == null || originalValue < 0) {
            return null;
        }
        Enum<?>[] enums = INT_INDEX_CACHE.get(type);
        if (enums == null) {
            synchronized (INT_INDEX_CACHE) {
                enums = INT_INDEX_CACHE.get(type);
                if (enums == null) {
                    // 类型不正确, 直接异常啦
                    Class<T> clazz = ObjectUtils.typeCast(ClassUtils.forName(type.getTypeName()));
                    enums = clazz.getEnumConstants();
                    INT_INDEX_CACHE.put(type, enums);
                }
            }
        }
        if (originalValue > enums.length - 1) {
            return null;
        }
        return ObjectUtils.typeCast(enums[originalValue]);
    }

    /**
     * String转枚举
     * @param originalValue 原始值
     * @param clazz 枚举类
     * @param <T> 枚举类型
     * @return 枚举对象
     */
    @Nullable
    public static <T extends Enum<T>> Enum<T> toEnum(@Nullable String originalValue, Class<T> clazz) {
        return toEnum(originalValue, (Type) clazz);
    }

    /**
     * String转枚举
     * @param originalValue 原始值
     * @param type 枚举类
     * @param <T> 枚举类型
     * @return 枚举对象
     */
    @Nullable
    public static <T extends Enum<T>> Enum<T> toEnum(@Nullable String originalValue, Type type) {
        Map<String, Enum<?>> enumMap = STR_INDEX_CACHE.get(type);
        if (enumMap == null) {
            synchronized (STR_INDEX_CACHE) {
                enumMap = STR_INDEX_CACHE.get(type);
                if (enumMap == null) {
                    enumMap = new HashMap<>();
                    // 类型不正确, 直接异常啦
                    Class<T> clazz = ObjectUtils.typeCast(ClassUtils.forName(type.getTypeName()));
                    for (Enum<T> anEnum : clazz.getEnumConstants()) {
                        enumMap.put(anEnum.name(), anEnum);
                    }
                    STR_INDEX_CACHE.put(type, enumMap);
                }
            }
        }
        return ObjectUtils.typeCastNullable(enumMap.get(originalValue));
    }
}
