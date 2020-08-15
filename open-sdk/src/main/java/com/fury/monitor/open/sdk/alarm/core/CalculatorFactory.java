package com.okcoin.cloud.client.demo.alarm.core;


import java.util.Collection;
import java.util.Map;

import com.google.common.collect.Maps;

import static com.okcoin.cloud.client.demo.alarm.core.Algorithm.AVG;

/**
 * @author : fury
 * @date : 2020/8/7
 */
public class CalculatorFactory {

    private final Map<Algorithm, ICalculator> calculatorMap = Maps.newHashMap();

    public Collection<ICalculator> getCalculators() {
        return calculatorMap.values();
    }

    public void compute(final String mixId, final CalculateParams params) {
        final Algorithm algorithm = params.getAlgorithm();
        final ICalculator iCalculator = calculatorMap.get(algorithm);
        final Integer unixMin = TimeUtil.nowMin().intValue();
        if (AVG.equals(algorithm)) {
            final AvgData avgData = new AvgData(params.getNumerator(), params.getDenominator());
            iCalculator.compute(unixMin, mixId, avgData);
        } else {
            iCalculator.compute(unixMin, mixId, params.getValue());
        }
    }

    private CalculatorFactory() {
        calculatorMap.put(Algorithm.SUM, SumCalculator.getInstance());
        calculatorMap.put(Algorithm.MAX, MaxCalculator.getInstance());
        calculatorMap.put(Algorithm.MIN, MinCalculator.getInstance());
        calculatorMap.put(AVG, AvgCalculator.getInstance());
    }

    private static final CalculatorFactory ourInstance = new CalculatorFactory();

    public static CalculatorFactory getInstance() {
        return ourInstance;
    }

}
