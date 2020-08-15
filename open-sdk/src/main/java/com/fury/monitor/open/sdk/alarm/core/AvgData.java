package com.okcoin.cloud.client.demo.alarm.core;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author : fury
 * @date : 2020/8/7
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AvgData {

    private int numerator;
    private int denominator;

    public void put(final int numerator, final int denominator) {
        this.numerator += numerator;
        this.denominator += denominator;
    }

}
