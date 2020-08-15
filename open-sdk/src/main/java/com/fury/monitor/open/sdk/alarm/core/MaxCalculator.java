package com.okcoin.cloud.client.demo.alarm.core;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections4.MapUtils;

import com.okcoin.cloud.client.demo.alarm.task.RiskMonitorConsumer;

/**
 * @author : fury
 * @date : 2020/8/7
 */
public class MaxCalculator implements ICalculator<String, Integer> {

    private final Object lock = new Object();
    private static final MaxCalculator ourInstance = new MaxCalculator();

    public static MaxCalculator getInstance() {
        return ourInstance;
    }

    private final HashMap<Integer, Map<String, Integer>> timeMaxMap = new HashMap<>();
    private final RiskMonitorConsumer consumer = RiskMonitorConsumer.getInstance();

    @Override
    public void compute(final Integer unixMin, final String mixId, final Integer value) {
        synchronized (lock) {
            Map<String, Integer> maxMap = timeMaxMap.get(unixMin);
            if (MapUtils.isEmpty(maxMap)) {
                maxMap = new HashMap<>();
                maxMap.put(mixId, value);
                timeMaxMap.put(unixMin, maxMap);
            } else {
                maxMap.compute(mixId, (key, valueCache) -> {
                    if (null == valueCache) {
                        return value;
                    }
                    return value > valueCache ? value : valueCache;
                });
            }
        }

    }

    @Override
    public Map<String, Integer> getCache(final Integer unixMin) {
        final Map<String, Integer> cache;
        synchronized (lock) {
            cache = timeMaxMap.remove(unixMin);
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
            consumer.consume(unixMin, cache, Algorithm.MAX);
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }
}
