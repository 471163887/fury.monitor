package com.okcoin.cloud.client.demo.alarm.task;


import java.util.Map;

import com.okcoin.cloud.client.demo.alarm.core.Algorithm;
import com.okcoin.cloud.client.demo.alarm.core.AvgData;
import com.okcoin.cloud.client.demo.alarm.core.ICacheConsumer;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.StringUtils;

import com.google.common.collect.Maps;
import com.okcoin.commons.lang.util.DoubleUtil;


import lombok.extern.slf4j.Slf4j;

import static com.okcoin.cloud.client.demo.alarm.task.ThreadPoolManager.ALARM_EVENT_ES;

/**
 * @author : fury
 * @date : 2020/8/7
 */
@Slf4j
public class RiskMonitorConsumer implements ICacheConsumer {

    private static final RiskMonitorConsumer ourInstance = new RiskMonitorConsumer();

    public static RiskMonitorConsumer getInstance() {
        return ourInstance;
    }

    private final Map<String, String> alarmMap = Maps.newHashMap();

    private RiskMonitorConsumer() {
        loadAlarmConfig();
    }

    private void loadAlarmConfig() {
        alarmMap.put("", "3");
        alarmMap.put("testsum", "5");
    }

    @Override
    public void consume(final Integer unixMin, final Map<String, Integer> cache, final Algorithm algorithm) {
        if (MapUtils.isEmpty(cache)) {
            return;
        }
        log.info("[RiskMonitorConsumer_consume] unixMin:{},cache:{}", unixMin, cache.toString());
        for (final Map.Entry<String, Integer> entry : cache.entrySet()) {

            try {
                final String key = entry.getKey();
                final Integer value = entry.getValue();
                final String valueStr = alarmMap.get(key);
                if (StringUtils.isEmpty(valueStr)) {
                    continue;
                }
                final Integer alarmValue = Integer.parseInt(valueStr);
                if (value >= alarmValue) {
                    final AlarmEventTask eventTask = AlarmEventTask.builder().unixMin(unixMin).key(key).value(value)
                        .alarmValue(alarmValue).algorithm(algorithm).build();
                    ALARM_EVENT_ES.submit(eventTask);
                }
            } catch (final Throwable ignored) {

            }

        }
    }

    public void consumeAvg(final Integer unixMin, final Map<String, AvgData> cache) {
        if (MapUtils.isEmpty(cache)) {
            return;
        }
        log.info("[RiskMonitorConsumer_consumeAvg]cache:{}", cache.toString());
        for (final Map.Entry<String, AvgData> entry : cache.entrySet()) {
            final String key = entry.getKey();
            final String valueStr = alarmMap.get(key);
            if (StringUtils.isEmpty(valueStr)) {
                continue;
            }
            final Double alarmValue = Double.valueOf(valueStr);

            final AvgData avgData = entry.getValue();
            final Double numerator = new Double(avgData.getNumerator());
            final Double denominator = new Double(avgData.getDenominator());
            final Double value = DoubleUtil.divide(numerator, denominator, 2);

            if (value >= alarmValue) {
                final AlarmEventTask alarmEventTask = AlarmEventTask.builder().unixMin(unixMin).key(key).doubleValue(value)
                    .doubleAlarmValue(alarmValue).algorithm(Algorithm.AVG).build();
                ALARM_EVENT_ES.submit(alarmEventTask);
            }
        }
    }

}