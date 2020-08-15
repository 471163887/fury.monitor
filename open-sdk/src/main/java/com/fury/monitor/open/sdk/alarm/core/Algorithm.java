package com.okcoin.cloud.client.demo.alarm.core;

import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * @author : fury
 * @date : 2020/8/7
 */
public enum Algorithm {

    // 监控项类型
    UNKNOWN(0, "无效类型"), SUM(1, "sum"), MAX(2, "max"), MIN(3, "min"), AVG(4, "avg");

    int code;
    String desc;

    Algorithm(final int code, final String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static Algorithm codeOf(final int code) {
        final Algorithm[] values = Algorithm.values();
        final Predicate<Algorithm> predicate = type -> type.getCode() == code;
        return Stream.of(values).filter(predicate).findFirst().orElse(UNKNOWN);
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
