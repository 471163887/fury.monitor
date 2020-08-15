package com.okcoin.cloud.client.demo.alarm.core;

import java.util.Collection;

/**
 * 负责计算及缓存监控项值
 * 
 * @author : fury
 * @date : 2020/8/7
 */
public class CalculatorManager {

    private final CalculatorFactory calculatorFactory = CalculatorFactory.getInstance();

    public void calculate(final CalculateParams params) {
        calculatorFactory.compute(params.getItemName(), params);
    }

    public void report(final Integer unixMin) {
        final Collection<ICalculator> iCalculators = calculatorFactory.getCalculators();
        for (final ICalculator iCalculator : iCalculators) {
            iCalculator.report(unixMin);
        }
    }

    private static final CalculatorManager ourInstance = new CalculatorManager();

    public static CalculatorManager getInstance() {
        return ourInstance;
    }

}
