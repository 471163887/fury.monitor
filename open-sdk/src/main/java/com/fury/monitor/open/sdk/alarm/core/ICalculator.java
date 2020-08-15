package com.okcoin.cloud.client.demo.alarm.core;

import java.util.Map;

/**
 * @author : fury
 * @date : 2020/8/7
 */
public interface ICalculator<String, T> {

    void compute(Integer unixMin, String mixId, T value);

    Map<String, T> getCache(Integer unixMin);

    void report(Integer unixMin);
}
