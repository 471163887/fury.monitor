package com.okcoin.cloud.client.demo.alarm.core;


import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;


import lombok.extern.slf4j.Slf4j;

import static com.okcoin.cloud.client.demo.alarm.core.CoreConstants.FURY_MONITOR;
import static com.okcoin.cloud.client.demo.alarm.core.CoreConstants.REPORT_INTERVAL;

/**
 * @author : fury
 * @date : 2020/8/7
 */
@Slf4j
public class CoreScheduleTask {

    private static final ScheduledExecutorService scheduledExecutor =
        Executors.newScheduledThreadPool(8, new ThreadFactory() {
            @Override
            public Thread newThread(final Runnable runnable) {
                final Thread thread = Executors.defaultThreadFactory().newThread(runnable);
                thread.setName("report-thread-pool");
                thread.setDaemon(true);
                return thread;
            }
        });

    /**
     * 监控项1分钟上报一次。进程退出时执行最后一次上报。
     */
    public static void init() {
        final LocalDateTime now = LocalDateTime.now();
        final LocalDate localDate = LocalDate.now();
        final LocalTime localTime = LocalTime.of(now.getHour(), now.getMinute() + 1, 10);
        final LocalDateTime next = LocalDateTime.of(localDate, localTime);
        final Long nowMs = Instant.now().toEpochMilli();
        final Long nextMs = TimeUtil.localDateTime2Ms(next);
        final long initialDelay = nextMs - nowMs;
        log.info(FURY_MONITOR + "initialDelay:{}", initialDelay);
        scheduledExecutor.scheduleWithFixedDelay(new ReportTask(), initialDelay, REPORT_INTERVAL,
            TimeUnit.MILLISECONDS);
        Runtime.getRuntime().addShutdownHook(new Thread(new ReportTask()));
    }

    public static synchronized void close() {
        if (null != scheduledExecutor) {
            scheduledExecutor.shutdown();
        }
    }

}
