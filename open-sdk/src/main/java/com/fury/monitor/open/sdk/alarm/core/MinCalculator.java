package com.okcoin.cloud.client.demo.alarm.core;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections4.MapUtils;

import com.okcoin.cloud.client.demo.alarm.task.RiskMonitorConsumer;

/**
 * @author : fury
 * @date : 2020/8/7
 */
public class MinCalculator implements ICalculator<String, Integer> {

    private final Object lock = new Object();
    private static final MinCalculator ourInstance = new MinCalculator();

    public static MinCalculator getInstance() {
        return ourInstance;
    }

    private final HashMap<Integer, HashMap<String, Integer>> timeMinMap = new HashMap<>();
    private final RiskMonitorConsumer consumer = RiskMonitorConsumer.getInstance();

    @Override
    public void compute(final Integer unixMin, final String mixId, final Integer value) {
        synchronized (lock) {
            HashMap<String, Integer> minMap = timeMinMap.get(unixMin);
            if (MapUtils.isEmpty(minMap)) {
                minMap = new HashMap<>();
                minMap.put(mixId, value);
                timeMinMap.put(unixMin, minMap);
            } else {
                minMap.compute(mixId, (key, valueCache) -> {
                    if (null == valueCache) {
                        return value;
                    }
                    return value < valueCache ? value : valueCache;
                });
            }
        }
    }

    @Override
    public Map<String, Integer> getCache(final Integer unixMin) {
        final Map<String, Integer> cache;
        synchronized (lock) {
            cache = timeMinMap.remove(unixMin);
        }
        if (MapUtils.isEmpty(cache)) {
            return null;
        }
        return cache;
    }

    @Override
    public void report(final Integer unixMin) {
        final Map<String, Integer> cache = getCache(unixMin);
        try {
            consumer.consume(unixMin, cache, Algorithm.MIN);
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }
}
