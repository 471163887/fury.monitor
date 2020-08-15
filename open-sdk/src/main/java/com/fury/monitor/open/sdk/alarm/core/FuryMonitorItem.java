package com.okcoin.cloud.client.demo.alarm.core;

import java.lang.annotation.*;

/**
 * 监控项：1.调用次数_count 2.异常次数_exception 3.方法最大耗时_max_time(单位毫秒) 4.平均耗时_avg_time(单位毫秒)
 * 以name为统计名称前缀，方法名为标签，统计上面四项；如果监控项名为空，则以类名为监控项 该注解可以用在类上或方法上。
 * 
 * @author : fury
 * @date : 2020/8/7
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface FuryMonitorItem {
    /**
     * 监控项目模块名称， 如果不配置，将以被注解的类的类名作为模块名
     */
    String name() default "";
}
