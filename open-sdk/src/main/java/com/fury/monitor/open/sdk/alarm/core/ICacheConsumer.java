package com.okcoin.cloud.client.demo.alarm.core;

import java.util.Map;

/**
 * @author : fury
 * @date : 2020/8/7
 */
public interface ICacheConsumer {

    /**
     * 处理分钟级别的监控数据。
     */
    void consume(Integer unixMin, Map<String, Integer> cache, Algorithm algorithm);

}
