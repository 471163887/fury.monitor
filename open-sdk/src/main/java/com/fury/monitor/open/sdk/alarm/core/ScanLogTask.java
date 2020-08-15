package com.okcoin.cloud.client.demo.alarm.core;

import static com.okcoin.cloud.client.demo.alarm.core.CoreConstants.EXCEPTION_CLASS_PATTERN;
import static com.okcoin.cloud.client.demo.alarm.core.CoreConstants.TIME_FORMATTER_MIN;
import static com.okcoin.cloud.client.demo.alarm.core.LogScanner.ITEM_NAME;
import static com.okcoin.cloud.client.demo.alarm.core.LogScanner.lastTimeFileSize;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.util.regex.Matcher;

import org.apache.commons.lang3.StringUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * @author : fury
 * @date : 2020/8/7
 */
@Slf4j
public class ScanLogTask implements Runnable {

    private final File logFile;

    private Integer logCount = 0;

    ScanLogTask(final File logFile) {
        this.logFile = logFile;
    }

    @Override
    public void run() {
        final LocalTime now = LocalTime.now();
        final LocalTime beforeNow = now.minusMinutes(1);
        final String nowMin = now.format(TIME_FORMATTER_MIN);
        final String beforeMin = beforeNow.format(TIME_FORMATTER_MIN);
        try {
            final long len = logFile.length();
            if (len < lastTimeFileSize) {
                lastTimeFileSize = 0L;
            } else {
                final RandomAccessFile randomFile = new RandomAccessFile(logFile, "rw");
                randomFile.seek(lastTimeFileSize);
                String line;
                boolean lastFind = false; // 记录上一行是否匹配到了ERROR日志
                String tag; // 记录当前行匹配到的异常类。比如a.b.c.ExampleException，则得到Example tag
                // 匹配ERROR日志，并且在下一行匹配这个ERROR对应的异常类。适用于log.error(..., Exception e)
                while ((line = randomFile.readLine()) != null) {
                    tag = matchExceptionTag(line);
                    if (isNewErrorCount(line, nowMin, beforeMin)) {
                        if (lastFind) {
                            // 当前行出现ERROR,并且上一行也出现ERROR.说明上一行ERROR没有匹配到异常类
                            FuryMonitor.sum(ITEM_NAME, 1, "");
                        }
                        lastFind = true; // 下一行匹配异常类
                    } else {
                        if (lastFind) {
                            if (tag != null) {
                                FuryMonitor.sum(ITEM_NAME, 1, tag);
                            } else {
                                // 上一行出现error,这一行没有匹配到异常类。异常类标签在出现error的下一行匹配
                                FuryMonitor.sum(ITEM_NAME, 1, "");
                            }
                            lastFind = false;
                        }
                    }

                }
                if (lastFind) {
                    FuryMonitor.sum(ITEM_NAME, 1, "");
                }
                lastTimeFileSize = randomFile.length();
                randomFile.close();
            }
        } catch (final Exception e) {
            ++logCount;
            if (logCount % 300 == 0) {
                log.error("[ARCH_ZMONITOR_CLIENT_ScanLogTask_error]扫描文件{}异常，当前位置{}", logFile.getName(),
                    lastTimeFileSize, e);
            }
        }
    }

    private static String matchExceptionTag(String str) {
        if (StringUtils.isEmpty(str)) {
            return null;
        }
        // 如果字符串过长，正则匹配耗时会比较多
        if (str.length() > 120) {
            str = str.substring(0, 120);
        }
        Matcher matcher = EXCEPTION_CLASS_PATTERN.matcher(str);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private boolean isNewErrorCount(final String str, final String nowMin, final String beforeMin) {
        if (StringUtils.isEmpty(str)) {
            return false;
        }
        try {
            final String oneLine = new String(str.getBytes(StandardCharsets.UTF_8));
            final int length = oneLine.length();
            final int end = Math.min(length, 50); // 取前50字符串
            final String line = oneLine.substring(0, end);
            final boolean hasError = line.contains("ERROR");
            final boolean hasTime = line.contains(nowMin) || line.contains(beforeMin);
            return hasError && hasTime;
        } catch (final Exception e) {
            log.error("[ARCH_ZMONITOR_CLIENT_isNewErrorCount]判断是否是ERROR级别日志异常", e);
            return false;
        }
    }

}
