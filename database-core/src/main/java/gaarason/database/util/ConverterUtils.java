package gaarason.database.util;

import gaarason.database.appointment.FinalVariable;
import gaarason.database.exception.TypeCastException;
import gaarason.database.exception.TypeNotSupportedException;
import gaarason.database.lang.Nullable;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.*;
import java.text.NumberFormat;
import java.text.ParseException;
import java.time.*;
import java.util.Date;
import java.util.Optional;

/**
 * 类型转化类
 * @author xt
 */
public final class ConverterUtils {

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
     * @throws TypeNotSupportedException 不支持的类型
     * @throws NumberFormatException 非数字格式
     */
    public static Number getAsNumber(final Object obj) throws TypeNotSupportedException, NumberFormatException {
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
            throw new TypeNotSupportedException();
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
     * 对象转Blob
     * @param obj 对象
     * @return Blob
     * @throws TypeNotSupportedException 不支持的类型
     */
    public static Blob getAsBlob(final Object obj) throws TypeNotSupportedException {
        if (obj instanceof Blob) {
            return (Blob) obj;
        }
        throw new TypeNotSupportedException();
    }

    /**
     * 对象转Clob
     * @param obj 对象
     * @return Clob
     * @throws TypeNotSupportedException 不支持的类型
     */
    public static Clob getAsClob(final Object obj) throws TypeNotSupportedException {
        if (obj instanceof Clob) {
            return (Clob) obj;
        }
        throw new TypeNotSupportedException();
    }

    /**
     * 对象转指定类型
     * @param obj 对象
     * @param clz 类型
     * @return 对应类型的数据
     * @throws TypeCastException 类型转化失败
     * @see FinalVariable ALLOW_FIELD_TYPES
     */
    @Nullable
    public static <R> R castNullable(@Nullable final Object obj, final Class<R> clz) throws TypeCastException {
        if (obj == null) {
            return null;
        } else if (obj instanceof Optional) {
            Optional<?> optionalObj = (Optional<?>) obj;
            return optionalObj.map(o -> cast(o, clz)).orElse(null);
        } else {
            return cast(obj, clz);
        }
    }

    /**
     * 对象转指定类型
     * @param obj 对象
     * @param clz 类型
     * @return 对应类型的数据
     * @throws TypeCastException 类型转化失败
     * @see FinalVariable ALLOW_FIELD_TYPES
     */
    public static <R> R cast(final Object obj, final Class<R> clz) throws TypeCastException {
        // 无需转化
        if(clz.isAssignableFrom(obj.getClass())){
            return ObjectUtils.typeCast(obj);
        }

        Object result;
        if (Boolean.class.equals(clz) || boolean.class.equals(clz)) {
            result = getAsBoolean(obj);
        } else if (Byte.class.equals(clz) || byte.class.equals(clz)) {
            result = getAsByte(obj);
        } else if (Character.class.equals(clz) || char.class.equals(clz)) {
            result = getAsString(obj).toCharArray()[0];
        } else if (Short.class.equals(clz) || short.class.equals(clz)) {
            result = getAsShort(obj);
        } else if (Integer.class.equals(clz) || int.class.equals(clz)) {
            result = getAsInteger(obj);
        } else if (Long.class.equals(clz) || long.class.equals(clz)) {
            result = getAsLong(obj);
        } else if (Float.class.equals(clz) || float.class.equals(clz)) {
            result = getAsFloat(obj);
        } else if (Double.class.equals(clz) || double.class.equals(clz)) {
            result = getAsDouble(obj);
        } else if (BigInteger.class.isAssignableFrom(clz)) {
            result = getAsBigInteger(obj);
        } else if (BigDecimal.class.equals(clz)) {
            result = getAsBigDecimal(obj);
        } else if (Number.class.isAssignableFrom(clz)) {
            result = getAsNumber(obj);
        } else if (java.sql.Date.class.equals(clz)) {
            result = java.sql.Date.valueOf(LocalDateUtils.str2LocalDate(getAsString(obj)));
        } else if (Time.class.equals(clz)) {
            result = Time.valueOf(LocalDateUtils.str2LocalTime(getAsString(obj)));
        } else if (Year.class.equals(clz)) {
            result = Year.from(LocalDateUtils.str2LocalDateTime(getAsString(obj)));
        } else if (YearMonth.class.equals(clz)) {
            result = YearMonth.from(LocalDateUtils.str2LocalDateTime(getAsString(obj)));
        } else if (Month.class.equals(clz)) {
            result = Month.from(LocalDateUtils.str2LocalDateTime(getAsString(obj)));
        } else if (MonthDay.class.equals(clz)) {
            result = MonthDay.from(LocalDateUtils.str2LocalDateTime(getAsString(obj)));
        } else if (Timestamp.class.equals(clz)) {
            result = Timestamp.valueOf(LocalDateUtils.str2LocalDateTime(getAsString(obj)));
        } else if (Date.class.isAssignableFrom(clz)) {
            result = LocalDateUtils.localDateTime2date(LocalDateUtils.str2LocalDateTime(getAsString(obj)));
        } else if (LocalDate.class.equals(clz)) {
            result = LocalDateUtils.str2LocalDate(getAsString(obj));
        } else if (LocalTime.class.equals(clz)) {
            result = LocalDateUtils.str2LocalTime(getAsString(obj));
        } else if (LocalDateTime.class.equals(clz)) {
            result = LocalDateUtils.str2LocalDateTime(getAsString(obj));
        } else if (String.class.equals(clz)) {
            result = getAsString(obj);
        } else if (Blob.class.isAssignableFrom(clz)) {
            result = getAsBlob(obj);
        } else if (Clob.class.isAssignableFrom(clz)) {
            result = getAsClob(obj);
        } else {
            result = obj;
        }
        return ObjectUtils.typeCast(result);
    }

    /**
     * 获取指定类型的默认值
     * @param clz 类型
     * @return 默认值
     * @throws TypeCastException 类型转换失败
     * @see FinalVariable ALLOW_FIELD_TYPES
     */
    @Nullable
    public static <R> R getDefaultValueByJavaType(Class<R> clz) throws TypeCastException {
        Object result;
        if (Boolean.class.equals(clz) || boolean.class.equals(clz)) {
            result = false;
        } else if (Byte.class.equals(clz) || byte.class.equals(clz)) {
            result = Byte.valueOf("0");
        } else if (Character.class.equals(clz) || char.class.equals(clz)) {
            result = ' ';
        } else if (Short.class.equals(clz) || short.class.equals(clz)) {
            result = Byte.valueOf("0");
        } else if (Integer.class.equals(clz) || int.class.equals(clz)) {
            result = Integer.valueOf("0");
        } else if (Long.class.equals(clz) || long.class.equals(clz)) {
            result = Long.valueOf("0");
        } else if (Float.class.equals(clz) || float.class.equals(clz)) {
            result = Float.valueOf("0");
        } else if (Double.class.equals(clz) || double.class.equals(clz)) {
            result = Double.valueOf("0");
        } else if (BigInteger.class.isAssignableFrom(clz)) {
            result = BigInteger.ZERO;
        } else if (BigDecimal.class.equals(clz)) {
            result = BigDecimal.valueOf(0L);
        } else if (Number.class.isAssignableFrom(clz)) {
            result = Long.valueOf("0");
        } else if (java.sql.Date.class.equals(clz)) {
            result = java.sql.Date.valueOf(LocalDateUtils.MIN_LOCAL_DATE_TIME.toLocalDate());
        } else if (Time.class.equals(clz)) {
            result = Time.valueOf(LocalDateUtils.MIN_LOCAL_DATE_TIME.toLocalTime());
        } else if (Year.class.equals(clz)) {
            result = Year.from(LocalDateUtils.MIN_LOCAL_DATE_TIME.toLocalTime());
        } else if (YearMonth.class.equals(clz)) {
            result = YearMonth.from(LocalDateUtils.MIN_LOCAL_DATE_TIME.toLocalTime());
        } else if (Month.class.equals(clz)) {
            result = Month.from(LocalDateUtils.MIN_LOCAL_DATE_TIME.toLocalTime());
        } else if (MonthDay.class.equals(clz)) {
            result = MonthDay.from(LocalDateUtils.MIN_LOCAL_DATE_TIME.toLocalTime());
        } else if (Timestamp.class.equals(clz)) {
            result = Timestamp.valueOf(LocalDateUtils.MIN_LOCAL_DATE_TIME);
        } else if (Date.class.isAssignableFrom(clz)) {
            result = LocalDateUtils.localDateTime2date(LocalDateUtils.MIN_LOCAL_DATE_TIME);
        } else if (LocalDate.class.equals(clz)) {
            result = LocalDateUtils.MIN_LOCAL_DATE_TIME.toLocalDate();
        } else if (LocalTime.class.equals(clz)) {
            result = LocalDateUtils.MIN_LOCAL_DATE_TIME.toLocalTime();
        } else if (LocalDateTime.class.equals(clz)) {
            result = LocalDateUtils.MIN_LOCAL_DATE_TIME;
        } else if (String.class.equals(clz)) {
            result = "";
        } else if (Blob.class.isAssignableFrom(clz)) {
            return null;
        } else if (Clob.class.isAssignableFrom(clz)) {
            return null;
        } else {
            return null;
        }
        return ObjectUtils.typeCast(result);
    }

    /**
     * 根据java类型，获取jdbc中的数据结果
     * @param field field
     * @param resultSet 结果集
     * @param column 列名
     * @return 值
     * @throws SQLException 数据库异常
     * @see FinalVariable ALLOW_FIELD_TYPES
     */
    @Nullable
    public static Object getValueFromJdbcResultSet(@Nullable Field field, ResultSet resultSet,
        String column) throws SQLException {
        // ModelShadowProvider 中没有指定的字段信息
        if (field == null) {
            return resultSet.getObject(column);
        }

        Class<?> fieldType = field.getType();

        Object value;

        if (Boolean.class.equals(fieldType) || boolean.class.equals(fieldType)) {
            value =  resultSet.getBoolean(column);
        } else if (Byte.class.equals(fieldType) || byte.class.equals(fieldType)) {
            value =  resultSet.getByte(column);
        } else if (Character.class.equals(fieldType) || char.class.equals(fieldType)) {
            String tempStr = resultSet.getString(column);
            value =  tempStr != null ? tempStr.toCharArray()[0] : ' ';
        } else if (Short.class.equals(fieldType) || short.class.equals(fieldType)) {
            value =  resultSet.getShort(column);
        } else if (Integer.class.equals(fieldType) || int.class.equals(fieldType)) {
            value =  resultSet.getInt(column);
        } else if (Long.class.equals(fieldType) || long.class.equals(fieldType)) {
            value =  resultSet.getLong(column);
        } else if (Float.class.equals(fieldType) || float.class.equals(fieldType)) {
            value =  resultSet.getFloat(column);
        } else if (Double.class.equals(fieldType) || double.class.equals(fieldType)) {
            value =  resultSet.getDouble(column);
        } else if (BigInteger.class.isAssignableFrom(fieldType)) {
            value =  new BigInteger(resultSet.getString(column));
        } else if (BigDecimal.class.equals(fieldType)) {
            value =  resultSet.getBigDecimal(column);
        } else if (Number.class.isAssignableFrom(fieldType)) {
            value =  resultSet.getLong(column);
        } else if (java.sql.Date.class.equals(fieldType)) {
            value =  resultSet.getDate(column);
        } else if (Time.class.equals(fieldType)) {
            value =  resultSet.getTime(column);
        } else if (Year.class.equals(fieldType)) {
            Timestamp timestamp = resultSet.getTimestamp(column);
            value =  ObjectUtils.isNull(timestamp) ? null : Year.from(timestamp.toLocalDateTime());
        } else if (YearMonth.class.equals(fieldType)) {
            Timestamp timestamp = resultSet.getTimestamp(column);
            value =  ObjectUtils.isNull(timestamp) ? null : YearMonth.from(timestamp.toLocalDateTime());
        } else if (Month.class.equals(fieldType)) {
            Timestamp timestamp = resultSet.getTimestamp(column);
            value =  ObjectUtils.isNull(timestamp) ? null : Month.from(timestamp.toLocalDateTime());
        } else if (MonthDay.class.equals(fieldType)) {
            Timestamp timestamp = resultSet.getTimestamp(column);
            value =  ObjectUtils.isNull(timestamp) ? null : MonthDay.from(timestamp.toLocalDateTime());
        } else if (Timestamp.class.equals(fieldType)) {
            value =  resultSet.getTimestamp(column);
        } else if (Date.class.isAssignableFrom(fieldType)) {
            Timestamp timestamp = resultSet.getTimestamp(column);
            value =  ObjectUtils.isNull(timestamp) ? null : Date.from(timestamp.toInstant());
        } else if (LocalDate.class.equals(fieldType)) {
            java.sql.Date date = resultSet.getDate(column);
            value =  ObjectUtils.isNull(date) ? null : date.toLocalDate();
        } else if (LocalTime.class.equals(fieldType)) {
            Time time = resultSet.getTime(column);
            value =  ObjectUtils.isNull(time) ? null : time.toLocalTime();
        } else if (LocalDateTime.class.equals(fieldType)) {
            Timestamp timestamp = resultSet.getTimestamp(column);
            value =  ObjectUtils.isNull(timestamp) ? null : timestamp.toLocalDateTime();
        } else if (String.class.equals(fieldType)) {
            value =  resultSet.getString(column);
        } else if (Blob.class.isAssignableFrom(fieldType)) {
            value =  resultSet.getBlob(column);
        } else if (Clob.class.isAssignableFrom(fieldType)) {
            value =  resultSet.getClob(column);
        } else {
            // 未识别的类型
            value =  resultSet.getObject(column, fieldType);
        }

        // 返回的字段值为null, 且目标类型可以接受null
        return resultSet.wasNull() && EntityUtils.isFieldCanBeNull(field) ? null : value;
    }
}
