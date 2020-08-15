package com.okcoin.cloud.client.demo.alarm.core;

import static com.okcoin.cloud.client.demo.alarm.core.CoreConstants.FURY_MONITOR;

import lombok.extern.slf4j.Slf4j;

/**
 * 每分钟的 10秒，检查前一分钟的所有数据
 * 
 * @author : fury
 * @date : 2020/8/7
 */
@Slf4j
public class ReportTask extends Thread {

    private final CalculatorManager calculatorManager = CalculatorManager.getInstance();

    @Override
    public void run() {
        final Integer nowMin = TimeUtil.nowMin().intValue() - 1;
        calculatorManager.report(nowMin);
    }

}
