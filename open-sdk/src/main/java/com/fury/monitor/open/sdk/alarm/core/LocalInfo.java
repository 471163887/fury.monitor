package com.okcoin.cloud.client.demo.alarm.core;

import static com.okcoin.cloud.client.demo.alarm.core.CoreConstants.FURY_MONITOR;

import org.apache.commons.lang3.StringUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * @author : fury
 * @date : 2020/8/7
 */
@Slf4j
public class LocalInfo {

    // 集群名称
    public static String clusterName = "";
    // 环境标签
    public static String envTag = "";

    public static void init(String clusterName, String envTag) {
        synchronized (LocalInfo.class) {
            if (StringUtils.isNotEmpty(LocalInfo.clusterName)) {
                return;
            }
            LocalInfo.clusterName = clusterName;
            LocalInfo.envTag = envTag;
            CoreScheduleTask.init();
            log.info(FURY_MONITOR + "集群初始化成功! clusterName={},envTag:{}", clusterName, envTag);
        }
    }

}
