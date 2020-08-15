package com.okcoin.cloud.client.demo.alarm.task;

import com.okcoin.cloud.client.demo.alarm.core.Algorithm;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author : fury
 * @date : 2020/8/7
 */
@Slf4j
@Data
@Builder
public class AlarmEventTask implements Runnable {

    private Integer unixMin;
    private String key;
    private Integer value;
    private Integer alarmValue;
    private Algorithm algorithm;
    private Double doubleValue;
    private Double doubleAlarmValue;

    @Override
    public void run() {
        log.info(this.toString());
    }

}
