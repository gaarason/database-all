package gaarason.database.util;

import gaarason.database.exception.TypeNotSupportedException;

import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Calendar;
import java.util.Date;

public interface DateTimeUtils {

    /**
     * 根据类型返回当前时间
     * @param type 类型
     * @param <T> 类型
     * @return 当前时间
     * @throws TypeNotSupportedException 类型不支持
     */
    @SuppressWarnings("unchecked")
    static <T> T currentDateTime(Class<?> type) throws TypeNotSupportedException {
        Object currentDateTime;
        if (Date.class.equals(type)) {
            currentDateTime = new Date();
        } else if (Calendar.class.equals(type)) {
            currentDateTime = Calendar.getInstance();
        } else if (LocalDate.class.equals(type)) {
            currentDateTime = LocalDate.now();
        } else if (LocalTime.class.equals(type)) {
            currentDateTime = LocalTime.now();
        } else if (LocalDateTime.class.equals(type)) {
            currentDateTime = LocalDateTime.now();
        } else if (java.sql.Date.class.equals(type)) {
            currentDateTime = new java.sql.Date(System.currentTimeMillis());
        } else if (Time.class.equals(type)) {
            currentDateTime = new Time(System.currentTimeMillis());
        } else if (Timestamp.class.equals(type)) {
            currentDateTime = new Timestamp(System.currentTimeMillis());
        } else {
            throw new TypeNotSupportedException(type);
        }
        return (T) currentDateTime;
    }
}
