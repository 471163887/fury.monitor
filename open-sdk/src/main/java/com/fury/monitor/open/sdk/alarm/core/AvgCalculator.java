package com.okcoin.cloud.client.demo.alarm.core;

import java.util.HashMap;
import java.util.Map;


import com.okcoin.cloud.client.demo.alarm.task.RiskMonitorConsumer;

/**
 * @author : fury
 * @date : 2020/8/7
 */
public class AvgCalculator implements com.okcoin.cloud.client.demo.alarm.core.ICalculator<String, com.okcoin.cloud.client.demo.alarm.core.AvgData> {

    private final Object lock = new Object();
    private static AvgCalculator ourInstance = new AvgCalculator();

    public static AvgCalculator getInstance() {
        return ourInstance;
    }

    private HashMap<Integer, HashMap<String, com.okcoin.cloud.client.demo.alarm.core.AvgData>> timeAvgMap = new HashMap<>();

    private RiskMonitorConsumer consumer = RiskMonitorConsumer.getInstance();

    @Override
    public void compute(Integer unixMin, String mixId, com.okcoin.cloud.client.demo.alarm.core.AvgData avgData) {
        synchronized (lock) {
            HashMap<String, com.okcoin.cloud.client.demo.alarm.core.AvgData> avgMap = timeAvgMap.get(unixMin);
            if (MapUtils.isEmpty(avgMap)) {
                avgMap = new HashMap<>();
                avgMap.put(mixId, avgData);
                timeAvgMap.put(unixMin, avgMap);
            } else {
                avgMap.merge(mixId, avgData, (cacheVal, newVal) -> {
                    int newNumerator = newVal.getNumerator() + cacheVal.getNumerator();
                    int newDenominator = newVal.getDenominator() + cacheVal.getDenominator();
                    return new com.okcoin.cloud.client.demo.alarm.core.AvgData(newNumerator, newDenominator);
                });
            }
        }

    }

    @Override
    public Map<String, com.okcoin.cloud.client.demo.alarm.core.AvgData> getCache(Integer unixMin) {
        final Map<String, com.okcoin.cloud.client.demo.alarm.core.AvgData> cache;
        synchronized (lock) {
            cache = timeAvgMap.remove(unixMin);
        }
        if (MapUtils.isEmpty(cache)) {
            return null;
        }
        return cache;
    }

    @Override
    public void report(Integer unixMin) {
        final Map<String, com.okcoin.cloud.client.demo.alarm.core.AvgData> cache = getCache(unixMin);
        try {
            consumer.consumeAvg(unixMin, cache);
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }
}
