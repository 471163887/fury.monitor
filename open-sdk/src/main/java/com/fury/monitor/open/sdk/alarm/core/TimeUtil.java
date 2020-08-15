package com.okcoin.cloud.client.demo.alarm.core;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/**
 *
 * @author : fury
 * @date : 2020/8/6
 */
public class TimeUtil {

    /**
     * 字符串时间格式
     */
    public static final String NORMAL_FORMATER = "yyyy-MM-dd HH:mm:ss";

    public static final String DATE_FORMATER = "yyyy-MM-dd";

    public static final String TIME_FORMATER = "HH:mm:ss";

    public static final String TIME_FORMATER_MIN = "HH:mm";

    public static final String MS_FORMATER = "yyyy-MM-dd HH:mm:ss.S";

    public static final String YEAR_MONTH = "yyyy-MM";

    public static final String STANDER_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";

    public static final String YYYY_MM_DD_HH_MM_SS_SSS = "yyyyMMddHHmmssSSS";

    /**
     * DateTimeFormatter作用：将 DateTime -> 字符串
     */
    public static final DateTimeFormatter DATE_MS_FORMATTER = DateTimeFormatter.ofPattern(YYYY_MM_DD_HH_MM_SS_SSS);

    public static final DateTimeFormatter FULL_FORMATTER = DateTimeFormatter.ofPattern(NORMAL_FORMATER);
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(DATE_FORMATER);

    public static final DateTimeFormatter MS_FORMATTER = DateTimeFormatter.ofPattern(MS_FORMATER);

    public static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern(TIME_FORMATER);

    public static final DateTimeFormatter TIME_FORMATTER_MIN = DateTimeFormatter.ofPattern(TIME_FORMATER_MIN);

    public static final DateTimeFormatter DATE_MONTH_FORMATTER = DateTimeFormatter.ofPattern(YEAR_MONTH);

    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(STANDER_FORMAT);

    /**
     * 1 分钟毫秒数
     */
    public static final int MINUTE_MS = 1000 * 60;

    /**
     * 1 小时毫秒数
     */
    public static final int HOUR_MS = MINUTE_MS * 60;

    /**
     * 1 天毫秒数
     */
    public static final int DAY_MS = HOUR_MS * 24;

    /**
     * 1 天秒数.
     */
    public static final long ONE_DAY_SECOND = 24 * 60 * 60;

    // ----- 字符串换Java时间对象 -----

    /**
     * 将 yyyy-MM-dd HH:mm:ss 换LocalDateTime.
     */
    public static LocalDateTime str2LocalDateTime(final String dateStr) {
        return LocalDateTime.parse(dateStr, FULL_FORMATTER);
    }

    /**
     * yyyy-MM-dd 换java原生的 Date.
     */
    public static Date dateStr2Date(final String dateStr) {
        final LocalDate localDate = LocalDate.parse(dateStr, DATE_FORMATTER);
        return localDate2Date(localDate);
    }

    /**
     * yyyy-MM-dd HH:mm:ss 换java原生的 Date.
     */
    public static Date timeStr2Date(final String timeStr) {
        final LocalDate localDate = LocalDate.parse(timeStr, FULL_FORMATTER);
        return localDate2Date(localDate);
    }

    /**
     * yyyy-MM-dd 换 LocalDate.
     */
    public static LocalDate dateStr2LocalDate(final String dateStr) {
        return LocalDate.parse(dateStr, DATE_FORMATTER);
    }

    // ----- Java时间对象换字符串 -----

    /**
     * localDateTime -> yyyy-MM-dd HH:mm:ss （常用）
     */
    public static String localDateTime2Str(final LocalDateTime localDateTime) {
        return localDateTime.format(FULL_FORMATTER);
    }

    /**
     * Date -> yyyy-MM-dd.
     */
    public static String date2Str(final Date date) {
        if (null == date) {
            return "";
        }
        final LocalDate localDate = date2LocalDate(date);
        return localDate.format(DATE_FORMATTER);
    }

    /**
     * instant -> yyyy-MM-dd HH:mm:ss.
     */
    public static String instant2Str(final Instant instant) {
        final LocalDateTime localDateTime = instant.atZone(ZoneId.systemDefault()).toLocalDateTime();
        return localDateTime.format(FULL_FORMATTER);
    }

    // ----- Java时间对象换Java时间对象 -----

    /**
     * LocalDateTime -> Date
     */
    public static Date localDateTime2Date(final LocalDateTime localDateTime) {
        final Instant instant = localDateTime.atZone(ZoneId.systemDefault()).toInstant();
        return Date.from(instant);
    }

    /**
     * Date -> LocalDateTime.
     */
    public static LocalDateTime date2LocalDateTime(final Date date) {
        if (null == date) {
            return null;
        }
        final ZonedDateTime zonedDateTime = date2ZonedDateTime(date);
        return zonedDateTime.toLocalDateTime();
    }

    /**
     * Date -> LocalDate.
     */
    public static LocalDate date2LocalDate(final Date date) {
        if (null == date) {
            return null;
        }
        final ZonedDateTime zonedDateTime = date2ZonedDateTime(date);
        return zonedDateTime.toLocalDate();
    }

    /**
     * Date -> ZonedDateTime.
     */
    private static ZonedDateTime date2ZonedDateTime(final Date date) {
        final Instant instant = date.toInstant();
        return instant.atZone(ZoneId.systemDefault());
    }

    /**
     * LocalDateTime -> Instant.
     */
    public static Instant localDateTime2Instant(final LocalDateTime localDateTime) {
        final ZoneOffset offset = OffsetDateTime.now().getOffset();
        return localDateTime.toInstant(offset);
    }

    /**
     * LocalDate -> Date.
     */
    public static Date localDate2Date(final LocalDate localDate) {
        final Instant instant = localDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        return Date.from(instant);
    }

    // ----- Java时间对象 -> 整型 -----

    /**
     * LocalDateTime -> 1970 年算起的毫秒数(ms)
     */
    public static Long localDateTime2Ms(final LocalDateTime localDateTime) {
        final Instant instant = localDateTime.atZone(ZoneId.systemDefault()).toInstant();
        return instant.toEpochMilli();
    }

    /**
     * LocalDateTime -> 1970 年算起的秒数(s) 实际的instant是包含了纳秒数，这个操作相当于忽略纳秒。
     */
    public static Long localDateTime2Second(final LocalDateTime localDateTime) {
        final Instant instant = localDateTime.atZone(ZoneId.systemDefault()).toInstant();
        return instant.getEpochSecond();
    }

    /**
     * Instant -> unix 分钟数。
     */
    public static int instant2Min(final Instant instant) {
        final Long epochSecond = instant.getEpochSecond() / 60;
        return epochSecond.intValue();
    }

    // ----- 整型 -> 字符串 -----

    /**
     * unix毫秒 -> yyyy-MM-dd HH:mm:ss
     */
    public static String unixMs2Str(final long unixMs) {
        final Instant instant = Instant.ofEpochMilli(unixMs);
        final LocalDateTime date = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        return date.format(FULL_FORMATTER);
    }

    /**
     * unix 分钟数 -> yyyy-MM-dd HH:mm:ss
     */
    public static String unixMin2Str(final Integer unixMin) {
        final int epoch = unixMin * 60;
        final Instant instant = Instant.ofEpochSecond(epoch);
        final LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        return localDateTime.format(FULL_FORMATTER);
    }

    // ----- 获取当前时间 -----

    /**
     * 当前时间字符串 yyyy-MM-dd HH:mm:ss格式.
     */
    public static String getNowString() {
        return LocalDateTime.now().format(FULL_FORMATTER);
    }

    /**
     * 当前时间 unix毫秒数
     */
    public static long now() {
        final LocalDateTime now = LocalDateTime.now();
        return localDateTime2Ms(now);
    }

    /**
     * 当前时间 unix分钟数
     */
    public static Long nowMin() {
        final Instant now = Instant.now();
        final long epochSecond = now.getEpochSecond();
        return epochSecond / 60;
    }

    /**
     * 当前时间 java date对象
     */
    public static Date nowDate() {
        final LocalDateTime now = LocalDateTime.now();
        return localDateTime2Date(now);
    }

    // ----- 特殊时间计算 -----

    /**
     * 要求定时任务在延迟执行，计算需要延迟的秒数.
     */
    public static long getInitDelay(final String time) {
        final LocalDate nowLocalDate = LocalDate.now();
        final LocalTime localTime = LocalTime.parse(time, TIME_FORMATTER);
        final LocalDateTime localDateTime = LocalDateTime.of(nowLocalDate, localTime);
        final LocalDateTime nowLocalDateTime = LocalDateTime.now();

        final Long second = localDateTime2Second(localDateTime);
        final Long nowSecond = localDateTime2Second(nowLocalDateTime);
        long initDelay = second - nowSecond;
        initDelay = initDelay > 0 ? initDelay : ONE_DAY_SECOND + initDelay;
        return initDelay;
    }

    /**
     * unix分钟数 和当前时间在 [-min, +min] min 分钟区间内
     */
    public static Boolean betweenMin(final Long unixMin, final int min) {
        final Long between = nowMin() - unixMin;
        return Math.abs(between) <= min;
    }

    /**
     * 当前分钟数 day 天前的分钟数
     */
    public static Integer nowMinBeforeDay(final int day) {
        final Long nowMin = nowMin();
        final int betweenMin = day * 24 * 60;
        final Long dayBefore = nowMin - betweenMin;
        return dayBefore.intValue();
    }

    /**
     * 根据日期获取 当天的起始分钟时刻. 查看一天监控图默认的起始时间
     */
    public static int getDayBeginMin(final String startTime) {
        final LocalDate localDate = dateStr2LocalDate(startTime);
        final LocalDateTime localDateTime = localDate.atStartOfDay();
        final Instant instant = localDateTime2Instant(localDateTime);
        return instant2Min(instant);
    }

    /**
     * 根据日期获取 当天的最后一分钟时刻. 查看一天监控图默认的结束时间
     */
    public static int getDayEndMin(final String time) {
        final LocalDate localDate = dateStr2LocalDate(time);
        LocalDateTime endTime = localDate.atTime(LocalTime.MAX);
        final LocalDateTime now = LocalDateTime.now();
        if (ChronoUnit.SECONDS.between(now, endTime) > 1) {
            endTime = now;
        }
        final Instant instant = localDateTime2Instant(endTime);
        return instant2Min(instant);
    }

}
