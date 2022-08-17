package com.nxtele.k8s.client.util;

import lombok.extern.slf4j.Slf4j;

import java.time.*;
import java.time.format.DateTimeFormatter;

/**
 * @author zhangjincheng
 */
@Slf4j
public class LocalDateTimeUtils {

    public static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("yyyy-MM");
    public static final DateTimeFormatter minuteFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    public static final DateTimeFormatter hourFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH");
    public static final DateTimeFormatter millisecondFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss:000");

    public static final LocalDateTime DEFAULT_TIME_1970 = LocalDateTime.of(1970, 1, 1, 0, 0);

    /**
     * 当天的开始/结束时间
     *
     * @param today
     * @param isFirst true 表示开始时间，false表示结束时间
     * @return
     */
    public static LocalDateTime getStartOrEndDayOfDay(LocalDate today, Boolean isFirst) {
        LocalDate resDate = LocalDate.now();
        if (today == null) {
            today = resDate;
        }
        if (isFirst) {
            return LocalDateTime.of(today, LocalTime.MIN);
        } else {
            return LocalDateTime.of(today, LocalTime.MAX);
        }
    }

    /**
     * 本周的开始、结束时间
     *
     * @param today
     * @param isFirst true 表示开始时间，false表示结束时间
     * @return
     */
    public static LocalDateTime getStartOrEndDayOfWeek(LocalDate today, Boolean isFirst) {
        LocalDate resDate = LocalDate.now();
        if (today == null) {
            today = resDate;
        }
        DayOfWeek week = today.getDayOfWeek();
        int value = week.getValue();
        if (isFirst) {
            return LocalDateTime.of(today.minusDays(value - 1), LocalTime.MIN);
        } else {
            return LocalDateTime.of(today.plusDays(7 - value), LocalTime.MAX);
        }
    }


    /**
     * 获取本月的开始/结束时间
     *
     * @param today
     * @param isFirst true 表示开始时间，false表示结束时间
     * @return
     */
    public static LocalDateTime getStartOrEndDayOfMonth(LocalDate today, Boolean isFirst) {
        LocalDate resDate = LocalDate.now();
        if (today == null) {
            today = resDate;
        }
        Month month = today.getMonth();
        int length = month.length(today.isLeapYear());
        if (isFirst) {
            return LocalDateTime.of(LocalDate.of(today.getYear(), month, 1), LocalTime.MIN);
        } else {
            return LocalDateTime.of(LocalDate.of(today.getYear(), month, length), LocalTime.MAX);
        }
    }

    /**
     * 本季度开始/结束时间
     *
     * @param today
     * @param isFirst true 表示开始时间，false表示结束时间
     * @return
     */
    public static LocalDateTime getStartOrEndDayOfQuarter(LocalDate today, Boolean isFirst) {
        LocalDate resDate = LocalDate.now();
        if (today == null) {
            today = resDate;
        }
        Month month = today.getMonth();
        Month firstMonthOfQuarter = month.firstMonthOfQuarter();
        Month endMonthOfQuarter = Month.of(firstMonthOfQuarter.getValue() + 2);
        if (isFirst) {
            return LocalDateTime.of(LocalDate.of(today.getYear(), firstMonthOfQuarter, 1), LocalTime.MIN);
        } else {
            return LocalDateTime.of(LocalDate.of(today.getYear(), endMonthOfQuarter, endMonthOfQuarter.length(today.isLeapYear())), LocalTime.MAX);
        }
    }

    /**
     * 本年度的开始/结束时间
     *
     * @param today
     * @param isFirst true 表示开始时间，false表示结束时间
     * @return
     */
    public static LocalDateTime getStartOrEndDayOfYear(LocalDate today, Boolean isFirst) {
        LocalDate resDate = LocalDate.now();
        if (today == null) {
            today = resDate;
        }
        if (isFirst) {
            return LocalDateTime.of(LocalDate.of(today.getYear(), Month.JANUARY, 1), LocalTime.MIN);
        } else {
            return LocalDateTime.of(LocalDate.of(today.getYear(), Month.DECEMBER, Month.DECEMBER.length(today.isLeapYear())), LocalTime.MAX);
        }
    }

    /**
     * 获取 N 天前的日期
     *
     * @param nDay      0表示今天 ，正数表示后N天(toDay + N) ， 负数表示前N天(toDay - N)
     * @param formatter 日期格式
     * @return
     */
    public static String getPreNDayString(int nDay, DateTimeFormatter formatter) {
        return LocalDateTime.now()
                .minusDays(-nDay)
                .format(formatter);
    }

    /**
     * 校验日期格式 yyyy-MM-dd
     *
     * @param date 日期字符串
     * @return
     */
    public static Boolean isDateVail(String date) {
        //用于指定 日期/时间 模式
        boolean flag = true;
        try {
            //Java 8 新添API 用于解析日期和时间
            LocalDate.parse(date, dateFormatter);
        } catch (Exception e) {
            e.printStackTrace();
            flag = false;
        }
        return flag;
    }

    /**
     * 日期转LocalDateTime
     *
     * @param dateStr 格式: yyyy-MM-dd HH:mm:ss
     * @return
     */
    public static LocalDateTime dateStrToLocalDateTime(String dateStr) {
        return LocalDateTime.parse(dateStr, dateTimeFormatter);
    }

    /**
     * 日期转LocalDate
     *
     * @param dateStr 格式: yyyy-MM-dd
     * @return
     */
    public static LocalDate dateStrToLocalDate(String dateStr) {
        return LocalDate.parse(dateStr, dateFormatter);
    }

    /**
     * 东八区当前时间戳
     *
     * @return
     */
    public static Long getCurrentTimeMillis() {
        return LocalDateTime.now().toEpochSecond(ZoneOffset.of("+8"));
    }

    /**
     * 时间戳转日期字符串
     *
     * @param timestamp
     * @return
     */
    public static String timeMillisToDateString(Long timestamp) {
        if (null == timestamp) {
            return null;
        }
        LocalDateTime localDateTime = LocalDateTime.ofEpochSecond(timestamp, 0, ZoneOffset.ofHours(8));
        return localDateTime.format(dateTimeFormatter);
    }
}
