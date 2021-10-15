package gaarason.database.util;

import gaarason.database.core.lang.Nullable;
import gaarason.database.exception.TypeCastException;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.NumberFormat;
import java.text.ParseException;

/**
 * 类型转化类
 * @author xt
 */
public class ConverterUtils {

    private ConverterUtils() {
    }

    /**
     * 对象转String
     * @param obj 对象
     * @return String
     */
    public static String getAsString(final Object obj) {
        return String.valueOf(obj);
    }

    /**
     * 对象转Number
     * @param obj 对象
     * @return Number
     */
    public static Number getAsNumber(final Object obj) {
        if (obj instanceof Number) {
            return (Number) obj;
        } else if (obj instanceof Boolean) {
            return Boolean.TRUE.equals(obj) ? 1 : 0;
        } else if (obj instanceof String) {
            try {
                return NumberFormat.getInstance().parse((String) obj);
            } catch (final ParseException e) {
                throw new NumberFormatException("For input string: \"" + obj + "\"");
            }
        } else {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * 对象转Boolean
     * @param obj 对象
     * @return Boolean
     */
    public static Boolean getAsBoolean(final Object obj) {
        if (obj instanceof Boolean) {
            return (Boolean) obj;
        } else if (obj instanceof String) {
            return Boolean.valueOf((String) obj);
        } else if (obj instanceof Number) {
            final Number n = (Number) obj;
            return (n.intValue() != 0) ? Boolean.TRUE : Boolean.FALSE;
        } else {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * 对象转boolean
     * @param obj 对象
     * @return boolean
     */
    public static boolean getAsBooleanValue(final Object obj) {
        return getAsBoolean(obj);
    }

    /**
     * 对象转Byte
     * @param obj 对象
     * @return Byte
     */
    public static Byte getAsByte(final Object obj) {
        return getAsNumber(obj).byteValue();
    }

    /**
     * 对象转byte
     * @param obj 对象
     * @return byte
     */
    public static byte getAsByteValue(final Object obj) {
        return getAsByte(obj);
    }

    /**
     * 对象转Short
     * @param obj 对象
     * @return Short
     */
    public static Short getAsShort(final Object obj) {
        return Short.valueOf(getAsString(obj));
    }

    /**
     * 对象转short
     * @param obj 对象
     * @return short
     */
    public static short getAsShortValue(final Object obj) {
        return Short.parseShort(getAsString(obj));
    }

    /**
     * 对象转Integer
     * @param obj 对象
     * @return Integer
     */
    public static Integer getAsInteger(final Object obj) {
        return getAsNumber(obj).intValue();
    }

    /**
     * 对象转int
     * @param obj 对象
     * @return int
     */
    public static int getAsIntValue(final Object obj) {
        return getAsInteger(obj);
    }

    /**
     * 对象转Long
     * @param obj 对象
     * @return Long
     */
    public static Long getAsLong(final Object obj) {
        return getAsNumber(obj).longValue();
    }

    /**
     * 对象转long
     * @param obj 对象
     * @return long
     */
    public static long getAsLongValue(final Object obj) {
        return getAsLong(obj);
    }

    /**
     * 对象转Float
     * @param obj 对象
     * @return Float
     */
    public static Float getAsFloat(final Object obj) {
        return getAsNumber(obj).floatValue();
    }

    /**
     * 对象转float
     * @param obj 对象
     * @return float
     */
    public static float getAsFloatValue(final Object obj) {
        return getAsFloat(obj);
    }

    /**
     * 对象转Double
     * @param obj 对象
     * @return Double
     */
    public static Double getAsDouble(final Object obj) {
        return getAsNumber(obj).doubleValue();
    }

    /**
     * 对象转double
     * @param obj 对象
     * @return double
     */
    public static double getAsDoubleValue(final Object obj) {
        return getAsDouble(obj);
    }

    /**
     * 对象转BigInteger
     * @param obj 对象
     * @return BigInteger
     */
    public static BigInteger getAsBigInteger(final Object obj) {
        if (obj instanceof BigInteger) {
            return (BigInteger) obj;
        } else if (obj instanceof String) {
            return new BigInteger((String) obj);
        } else {
            return new BigInteger(getAsString(getAsNumber(obj)));
        }
    }

    /**
     * 对象转BigDecimal
     * @param obj 对象
     * @return BigDecimal
     */
    public static BigDecimal getAsBigDecimal(final Object obj) {
        if (obj instanceof BigDecimal) {
            return (BigDecimal) obj;
        } else if (obj instanceof String) {
            return new BigDecimal((String) obj);
        } else {
            return BigDecimal.valueOf(getAsNumber(obj).doubleValue());
        }
    }


    /**
     * 对象转指定类型
     * @param obj 对象
     * @param clz 类型
     * @return 对应类型的数据
     * @throws TypeCastException 类型转化失败
     */
    @SuppressWarnings("unchecked")
    public static <R> R cast(final Object obj, final Class<R> clz) throws TypeCastException {
        R result = null;
        if (Boolean.class.equals(clz) || boolean.class.equals(clz)) {
            result = (R) getAsBoolean(obj);
        } else if (Byte.class.equals(clz) || byte.class.equals(clz)) {
            result = (R) getAsByte(obj);
        } else if (Short.class.equals(clz) || short.class.equals(clz)) {
            result = (R) getAsShort(obj);
        } else if (Integer.class.equals(clz) || int.class.equals(clz)) {
            result = (R) getAsInteger(obj);
        } else if (Long.class.equals(clz) || long.class.equals(clz)) {
            result = (R) getAsLong(obj);
        } else if (Float.class.equals(clz) || float.class.equals(clz)) {
            result = (R) getAsFloat(obj);
        } else if (Double.class.equals(clz) || double.class.equals(clz)) {
            result = (R) getAsDouble(obj);
        } else if (Character.class.equals(clz) || char.class.equals(clz)) {
            result = ObjectUtils.typeCast(getAsString(obj).toCharArray()[0]);
        } else if (String.class.equals(clz)) {
            result = (R) getAsString(obj);
        } else if (BigInteger.class.isAssignableFrom(clz)) {
            result = (R) getAsBigInteger(obj);
        } else if (BigDecimal.class.isAssignableFrom(clz)) {
            result = (R) getAsBigDecimal(obj);
        } else if (Number.class.isAssignableFrom(clz)) {
            result = (R) getAsNumber(obj);
        } else {
            result = ObjectUtils.typeCast(clz);
        }
        return result;
    }

    /**
     * 获取指定类型的默认值
     * @param clz 类型
     * @return 默认值
     */
    @Nullable
    public static Object getDefaultValueByJavaType(Class<?> clz) {
        if (Boolean.class.equals(clz) || boolean.class.equals(clz)) {
            return false;
        } else if (Byte.class.equals(clz) || byte.class.equals(clz)) {
            return Byte.valueOf("0");
        } else if (Short.class.equals(clz) || short.class.equals(clz)) {
            return Byte.valueOf("0");
        } else if (Integer.class.equals(clz) || int.class.equals(clz)) {
            return Integer.valueOf("0");
        } else if (Long.class.equals(clz) || long.class.equals(clz)) {
            return Long.valueOf("0");
        } else if (Float.class.equals(clz) || float.class.equals(clz)) {
            return Float.valueOf("0");
        } else if (Double.class.equals(clz) || double.class.equals(clz)) {
            return Double.valueOf("0");
        } else if (Character.class.equals(clz) || char.class.equals(clz)) {
            return ' ';
        } else if (BigInteger.class.equals(clz)) {
            return BigInteger.ZERO;
        } else if (String.class.equals(clz)) {
            return "";
        } else if (BigDecimal.class.equals(clz)) {
            return BigDecimal.valueOf(0L);
        } else {
            return null;
        }
    }
}
