package com.okcoin.cloud.client.demo.alarm.task;

import io.micrometer.core.instrument.util.NamedThreadFactory;

import java.util.concurrent.*;


/**
 * @author : fury
 * @date : 2020/8/7
 */
public class ThreadPoolManager {

    /**
     * 处理告警事件
     */
    public static final ExecutorService ALARM_EVENT_ES = initExecutor(8, 176, 511, "alarmEventEs");

    private static ThreadPoolExecutor initExecutor(final int corePoolSize, final int maximumPoolSize,
        final int queueSize, final String monitorKey) {
        final ArrayBlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<>(queueSize);
        final ThreadFactory threadFactory = new NamedThreadFactory(monitorKey);
        final ThreadPoolExecutor.DiscardOldestPolicy policy = new ThreadPoolExecutor.DiscardOldestPolicy();
        final ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, 0L,
            TimeUnit.MILLISECONDS, workQueue, threadFactory, policy);
        threadPoolExecutor.prestartCoreThread();
        return threadPoolExecutor;
    }

}
