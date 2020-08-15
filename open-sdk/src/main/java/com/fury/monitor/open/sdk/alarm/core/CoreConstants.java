package com.okcoin.cloud.client.demo.alarm.core;

import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

/**
 * @author : fury
 * @date : 2020/8/7
 */
public class CoreConstants {

    // 标签长度不能超过 127
    public static final int TAG_MAX_LEN = 127;

    // 监控项名称长度不能超过 255
    public static final int ITEM_MAX_LEN = 255;

    // 上报时间间隔
    public static final int REPORT_INTERVAL = 1000 * 60;

    public static final long LOG_LIMIT_TIME = 200L;

    public static final int QUEUE_BLOCK_PERCENT = 10;

    public static final String FURY_MONITOR = "[FuryMonitor]";

    public static final String TIME_FORMATER_MIN = "HH:mm";

    public static final DateTimeFormatter TIME_FORMATTER_MIN = DateTimeFormatter.ofPattern(TIME_FORMATER_MIN);

    public static final Pattern EXCEPTION_CLASS_PATTERN = Pattern.compile(".*\\.(.*)Exception.*");
}
