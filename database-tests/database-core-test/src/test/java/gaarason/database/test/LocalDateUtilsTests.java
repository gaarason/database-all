package gaarason.database.test;

import gaarason.database.util.LocalDateUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

@Slf4j
@FixMethodOrder(MethodSorters.JVM)
public class LocalDateUtilsTests {

    @Test
    public void testLocalDateUtilsDate2LocalDateTime() {
        String dateStringOne = "2020-11-11 11:41:01";
        String formatterString = "yyyy-MM-dd HH:mm:ss";

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        ParsePosition pos = new ParsePosition(0);
        Date dateOne = formatter.parse(dateStringOne, pos);
        final LocalDateTime localDateTimeOne = LocalDateUtils.date2LocalDateTime(dateOne);
        final String sOne = localDateTimeOne.format(DateTimeFormatter.ofPattern(formatterString));
        Assert.assertEquals(dateStringOne, sOne);
    }

    @Test
    public void testLocalDateUtilsDate2LocalDate() {
        String dateStringOne = "2099-01-11";
        String formatterString = "yyyy-MM-dd";

        SimpleDateFormat formatter = new SimpleDateFormat(formatterString);
        ParsePosition pos = new ParsePosition(0);
        Date dateOne = formatter.parse(dateStringOne, pos);
        final LocalDate localDateOne = LocalDateUtils.date2LocalDate(dateOne);
        final String sOne = localDateOne.format(DateTimeFormatter.ofPattern(formatterString));
        Assert.assertEquals(dateStringOne, sOne);
    }

    @Test
    public void testLocalDateUtilsDate2LocalTime() {
        String dateStringOne = "03:43:03";
        String formatterString = "HH:mm:ss";

        SimpleDateFormat formatter = new SimpleDateFormat(formatterString);
        ParsePosition pos = new ParsePosition(0);
        Date dateOne = formatter.parse(dateStringOne, pos);
        final LocalTime localTimeOne = LocalDateUtils.date2LocalTime(dateOne);
        final String sOne = localTimeOne.format(DateTimeFormatter.ofPattern(formatterString));
        Assert.assertEquals(dateStringOne, sOne);
    }

    @Test
    public void testLocalDateUtilsStr2LocalDateTime() {
        String dateStringOne = "2020-11-11 11:41:01";
        String formatterString = "yyyy-MM-dd HH:mm:ss";

        final LocalDateTime localDateTimeOne = LocalDateUtils.str2LocalDateTime(dateStringOne);
        final String sOne = localDateTimeOne.format(DateTimeFormatter.ofPattern(formatterString));
        Assert.assertEquals(dateStringOne, sOne);
    }


    @Test
    public void testLocalDateUtilsStr2LocalDate() {
        String dateStringOne = "2099-01-11";
        String formatterString = "yyyy-MM-dd";

        final LocalDate localDateOne = LocalDateUtils.str2LocalDate(dateStringOne);
        final String sOne = localDateOne.format(DateTimeFormatter.ofPattern(formatterString));
        Assert.assertEquals(dateStringOne, sOne);
    }

    @Test
    public void testLocalDateUtilsStr2LocalTime() {
        String dateStringOne = "03:43:03";
        String formatterString = "HH:mm:ss";

        final LocalTime localTimeOne = LocalDateUtils.str2LocalTime(dateStringOne);
        final String sOne = localTimeOne.format(DateTimeFormatter.ofPattern(formatterString));
        Assert.assertEquals(dateStringOne, sOne);
    }


    @Test
    public void testLocalDateUtilsLocalDateTime2date() {
        String dateStringOne = "2020-11-11 11:41:01";
        String formatterString = "yyyy-MM-dd HH:mm:ss";

        final LocalDateTime localDateTime = LocalDateTime.parse(dateStringOne,
            DateTimeFormatter.ofPattern(formatterString));
        final Date date = LocalDateUtils.localDateTime2date(localDateTime);
        SimpleDateFormat formatter = new SimpleDateFormat(formatterString);
        String sOne = formatter.format(date);
        Assert.assertEquals(dateStringOne, sOne);
    }

    @Test
    public void testLocalDateUtilsLocalDate2date() {
        String dateStringOne = "2099-01-11";
        String formatterString = "yyyy-MM-dd";

        final LocalDate localDate = LocalDate.parse(dateStringOne, DateTimeFormatter.ofPattern(formatterString));
        final Date date = LocalDateUtils.localDate2date(localDate);
        SimpleDateFormat formatter = new SimpleDateFormat(formatterString);
        String sOne = formatter.format(date);
        Assert.assertEquals(dateStringOne, sOne);
    }

    @Test
    public void testLocalDateUtilsLocalTime2date() {
        String dateStringOne = "03:43:03";
        String formatterString = "HH:mm:ss";

        final LocalTime localTime = LocalTime.parse(dateStringOne, DateTimeFormatter.ofPattern(formatterString));
        final Date date = LocalDateUtils.localTime2date(localTime);
        SimpleDateFormat formatter = new SimpleDateFormat(formatterString);
        String sOne = formatter.format(date);
        Assert.assertEquals(dateStringOne, sOne);
    }

}
