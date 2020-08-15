package com.okcoin.cloud.client.demo.alarm.core;

import java.io.File;
import java.util.concurrent.*;

/**
 * @author : fury
 * @date : 2020/8/7
 */
public class LogScanner {

    static String ITEM_NAME;
    static long lastTimeFileSize;
    private static ScheduledExecutorService logEs;
    private static final LogScanner ourInstance = new LogScanner();

    public static LogScanner getInstance() {
        return ourInstance;
    }

    public void monitorLog(final String filePath) {
        try {
            final ScanLogTask scanLogTask = initLogTask(filePath, LocalInfo.clusterName);
            runScanLogTask(scanLogTask);
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    private ScanLogTask initLogTask(final String filePath, final String clusterName) {
        ITEM_NAME = clusterName + "_error_log_count";
        lastTimeFileSize = 0;
        return new ScanLogTask(new File(filePath));
    }

    private void runScanLogTask(final ScanLogTask scanLogTask) {
        logEs.scheduleAtFixedRate(scanLogTask, 0, 1, TimeUnit.SECONDS);
    }

    private LogScanner() {
        logEs = new ScheduledThreadPoolExecutor(1, new ThreadFactory() {
            @Override
            public Thread newThread(final Runnable runnable) {
                final Thread thread = Executors.defaultThreadFactory().newThread(runnable);
                thread.setName("logEs");
                thread.setDaemon(true);
                return thread;
            }
        }, WorkThreadsPool.genDiscardOldestPolicy("logEs"));
    }

}
