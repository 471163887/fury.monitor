package com.okcoin.cloud.client.demo.alarm.core;

import static com.okcoin.cloud.client.demo.alarm.core.CoreConstants.FURY_MONITOR;

import org.apache.commons.lang3.StringUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * 直接根据item名称累计，本地缓存相当于队列角色。调用这个方法的线程为生产者。ReportTask是消费者，每次消费会清空缓存。
 * 
 * @author : fury
 * @date : 2020/8/7
 */
@Slf4j
public class FuryMonitor {

    private static final CalculatorManager calculatorManager = CalculatorManager.getInstance();

    public static void init(final String initClusterName, final String envTag) {
        final boolean isEmptyInput = StringUtils.isEmpty(initClusterName);
        if (isEmptyInput) {
            log.info(FURY_MONITOR + "集群名称initClusterName为空，初始化FuryMonitor失败。");
            return;
        }
        final boolean hasInit = StringUtils.isNotEmpty(LocalInfo.clusterName);
        if (hasInit) {
            log.info(FURY_MONITOR + "集群名称clusterName已初始化为:{}，不需要重复初始化。", LocalInfo.clusterName);
            return;
        }
        LocalInfo.init(initClusterName, envTag);
    }

    public static void checkErrorLog(final String filePath) {
        if (StringUtils.isEmpty(LocalInfo.clusterName)) {
            log.error("未初始化监控客户端，请先调用 Monitor.init(${集群名称}, ${环境标识})");
            return;
        }
        final LogScanner logScanner = LogScanner.getInstance();
        logScanner.monitorLog(filePath);
    }

    public static void sum(final String item, final Integer value) {
        calculate(item, value, null, Algorithm.SUM);
    }

    public static void sum(final String item, final Integer value, final String tag) {
        calculate(item, value, tag, Algorithm.SUM);
    }

    public static void max(final String item, final Integer value) {
        calculate(item, value, null, Algorithm.MAX);
    }

    public static void max(final String item, final Integer value, final String tag) {
        calculate(item, value, tag, Algorithm.MAX);
    }

    public static void min(final String item, final Integer value) {
        calculate(item, value, null, Algorithm.MIN);
    }

    public static void min(final String item, final Integer value, final String tag) {
        calculate(item, value, tag, Algorithm.MIN);
    }

    public static void avg(final String item, final Integer numerator, final Integer denominator) {
        final CalculateParams params =
            CalculateParams.Builder.buildAvg(LocalInfo.clusterName, item, numerator, denominator, null, Algorithm.AVG);
        if (null == params) {
            return;
        }
        calculatorManager.calculate(params);
    }

    public static void avg(final String item, final Integer numerator, final Integer denominator, final String tag) {
        final CalculateParams params =
            CalculateParams.Builder.buildAvg(LocalInfo.clusterName, item, numerator, denominator, tag, Algorithm.AVG);
        if (null == params) {
            return;
        }
        calculatorManager.calculate(params);
    }

    /**
     * 混合型的 监控项+标签
     * 
     * @param item
     *            监控项ID或者监控项名称
     * @param value
     *            数值： 最大、最小、求和
     * @param tag
     *            标签ID或者标签名称
     * @param algorithm
     *            算法：最大、最小、求和
     * @param item
     *            监控项名称
     * @param tag
     *            标签名称
     */
    private static void calculate(final String item, final Integer value, final String tag, final Algorithm algorithm) {
        final CalculateParams params =
            CalculateParams.Builder.build(LocalInfo.clusterName, item, value, tag, algorithm);
        if (null == params) {
            return;
        }
        calculatorManager.calculate(params);
    }

}
