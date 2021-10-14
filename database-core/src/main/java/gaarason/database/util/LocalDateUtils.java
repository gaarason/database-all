package gaarason.database.util;

import gaarason.database.exception.ReflectionNotSupportedException;
import gaarason.database.exception.TimeConversionException;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * 时间处理工具
 * @author xt
 */
public class LocalDateUtils {

    /**
     * 解决 SimpleDateFormat 线程不安全的问题, 使用 SIMPLE_DATE_FORMAT.get() 获取对象.
     */
    public static final ThreadLocal<SimpleDateFormat> SIMPLE_DATE_FORMAT = ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));

    /**
     * 自定义常用时间格式化字符串
     */
    private static final List<String> DATE_TIME_FORMATTER_CUSTOMIZE_DATETIME_STR_LIST = Arrays.asList("yyyy-MM-dd HH:mm:ss.SSS",
        "yyyy-MM-dd HH:mm:ss.SS",
        "yyyy-MM-dd HH:mm:ss.S", "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd", "HH:mm:ss.SSS", "HH:mm:ss.SS", "HH:mm:ss.S", "HH:mm:ss");

    /**
     * 时间戳最小日期
     */
    private static final LocalDate MIN = LocalDate.of(1970, 1, 1);

    /**
     * 可用的 DateTimeFormatter 列表
     */
    private static final List<DateTimeFormatter> DATE_TIME_FORMATTER_LIST = new ArrayList<>(24);

    static {
        for (String str : DATE_TIME_FORMATTER_CUSTOMIZE_DATETIME_STR_LIST) {
            DATE_TIME_FORMATTER_LIST.add(DateTimeFormatter.ofPattern(str));
        }
        DATE_TIME_FORMATTER_LIST.addAll(getDateTimeFormatterByReflection());
    }

    private LocalDateUtils() {
    }

    /**
     * Date转化为LocalDateTime
     * @param date Date
     * @return LocalDateTime
     */
    public static LocalDateTime date2LocalDateTime(Date date) {
        // 通过这种写法, 解决 Date 的子类中 toInstant() 禁用的问题.
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(date.getTime()), ZoneId.systemDefault());
    }

    /**
     * Date转化为LocalDate
     * @param date Date
     * @return LocalDate
     */
    public static LocalDate date2LocalDate(Date date) {
        return date2LocalDateTime(date).toLocalDate();
    }

    /**
     * Date转化为LocalTime
     * @param date Date
     * @return LocalTime
     */
    public static LocalTime date2LocalTime(Date date) {
        return date2LocalDateTime(date).toLocalTime();
    }


    /**
     * 时间字符串转化为LocalDateTime, 自动尝试多个时间格式
     * @param str 时间字符串
     * @return LocalDateTime
     * @throws TimeConversionException 时间转化异常
     */
    public static LocalDateTime str2LocalDateTime(String str) throws TimeConversionException {
        for (DateTimeFormatter formatter : DATE_TIME_FORMATTER_LIST) {
            try {
                return LocalDateTime.parse(str, formatter);
            } catch (Exception ignore) {
            }
        }
        throw new TimeConversionException(str);
    }

    /**
     * 时间字符串转化为LocalDate, 自动尝试多个时间格式
     * @param str 时间字符串
     * @return LocalDate
     */
    public static LocalDate str2LocalDate(String str) {
        for (DateTimeFormatter formatter : DATE_TIME_FORMATTER_LIST) {
            try {
                return LocalDate.parse(str, formatter);
            } catch (Exception ignore) {
            }
        }
        throw new TimeConversionException(str);
    }

    /**
     * 时间字符串转化为LocalTime, 自动尝试多个时间格式
     * @param str 时间字符串
     * @return LocalTime
     */
    public static LocalTime str2LocalTime(String str) {
        for (DateTimeFormatter formatter : DATE_TIME_FORMATTER_LIST) {
            try {
                return LocalTime.parse(str, formatter);
            } catch (Exception ignore) {
            }
        }
        throw new TimeConversionException(str);
    }

    /**
     * LocalDateTime转化为Date
     * @param localDateTime LocalDateTime
     * @return Date
     */
    public static Date localDateTime2date(LocalDateTime localDateTime) {
        Instant instant = localDateTime.atZone(ZoneId.systemDefault()).toInstant();
        return Date.from(instant);
    }

    /**
     * LocalDate转化为Date
     * @param localDate LocalDate
     * @return Date
     */
    public static Date localDate2date(LocalDate localDate) {
        Instant instant = localDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        return Date.from(instant);
    }

    /**
     * LocalTime转化为Date, 日期取 LocalDate.MIN
     * @param localTime LocalTime
     * @return Date
     */
    public static Date localTime2date(LocalTime localTime) {
        LocalDateTime localDateTime = LocalDateTime.of(MIN, localTime);
        return localDateTime2date(localDateTime);
    }

    /**
     * 通过反射, 将 DateTimeFormatter 中的预置格式获取, 并返回
     * @return DateTimeFormatter 列表
     */
    private static List<DateTimeFormatter> getDateTimeFormatterByReflection() {
        final ArrayList<DateTimeFormatter> list = new ArrayList<>();
        try {
            for (Field field : DateTimeFormatter.class.getDeclaredFields()) {
                // 是静态属性 且 类型是 DateTimeFormatter
                if (Modifier.isStatic(field.getModifiers()) && field.getType().equals(DateTimeFormatter.class)) {
                    list.add(ObjectUtils.typeCast(field.get(DateTimeFormatter.class)));
                }
            }
        } catch (Exception e) {
            throw new ReflectionNotSupportedException(e);
        }
        return list;
    }
}
