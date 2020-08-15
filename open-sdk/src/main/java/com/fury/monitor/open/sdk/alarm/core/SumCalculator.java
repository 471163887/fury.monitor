package com.okcoin.cloud.client.demo.alarm.core;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections4.MapUtils;

import com.okcoin.cloud.client.demo.alarm.task.RiskMonitorConsumer;

import lombok.extern.slf4j.Slf4j;

/**
 * @author : fury
 * @date : 2020/8/7
 */
@Slf4j
public class SumCalculator implements ICalculator<String, Integer> {

    private final Object lock = new Object();
    private final Map<Integer, HashMap<String, Integer>> timeSumMap = new HashMap<>();
    private static final RiskMonitorConsumer consumer = RiskMonitorConsumer.getInstance();
    private static final SumCalculator ourInstance = new SumCalculator();

    public static SumCalculator getInstance() {
        return ourInstance;
    }

    @Override
    public void compute(final Integer unixMin, final String mixId, final Integer value) {
        synchronized (lock) {
            HashMap<String, Integer> sumMap = timeSumMap.get(unixMin);
            if (MapUtils.isEmpty(sumMap)) {
                sumMap = new HashMap<>();
                sumMap.put(mixId, value);
                timeSumMap.put(unixMin, sumMap);
            } else {
                sumMap.merge(mixId, value, Integer::sum);
            }
        }
    }

    @Override
    public Map<String, Integer> getCache(final Integer unixMin) {
        final HashMap<String, Integer> cache;
        synchronized (lock) {
            cache = timeSumMap.remove(unixMin);
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
            consumer.consume(unixMin, cache, Algorithm.SUM);
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }
}
