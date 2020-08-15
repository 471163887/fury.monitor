package com.okcoin.cloud.client.demo.alarm.core;

import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

/**
 * 通过spring aop方式实现监控放的调用次数、异常次数、最大耗时、平均耗时。 参数和返回值也可以监控，目前只监控上面4项
 * 
 * @author : fury
 * @date : 2020/8/7
 */
@Aspect
@Component
public class FuryMonitorItemAspect {

    private static final Pattern compile = Pattern.compile(".*\\..*\\.(.*\\(.*)\\)");

    @Around("execution (* com..*.*(..)) && (@within(com.okcoin.cloud.client.demo.alarm.core.FuryMonitorItem) || @annotation(com.okcoin.cloud.client.demo.alarm.core.FuryMonitorItem))")
    public Object around(final ProceedingJoinPoint joinPoint) throws Throwable {
        final FuryMonitorItem furyMonitorItem = getFuryMonitorAnn(joinPoint);
        if (furyMonitorItem == null) {
            return joinPoint.proceed();
        }
        final String methodSignature = getMethodSignature(joinPoint);
        String itemName = furyMonitorItem.name();
        if (StringUtils.isEmpty(itemName)) {
            // 如果监控项名为空，则以类名为监控项
            itemName = LocalInfo.clusterName + "_" + joinPoint.getTarget().getClass().getSimpleName();
        } else {
            itemName = LocalInfo.clusterName + "_" + itemName;
        }
        // 监控调用次数
        FuryMonitor.sum(itemName + "_count", 1, methodSignature);
        final long time = System.currentTimeMillis();
        try {
            return joinPoint.proceed();
        } catch (final Throwable e) {
            // 异常次数+1
            FuryMonitor.sum(itemName + "_exception", 1, methodSignature);
            throw e;
        } finally {
            final int cost = (int)(System.currentTimeMillis() - time);
            // 统计方法平均耗时和最大耗时
            FuryMonitor.avg(itemName + "_avg_time", cost, 1, methodSignature);
            FuryMonitor.max(itemName + "_max_time", cost, methodSignature);
        }
    }

    private static FuryMonitorItem getFuryMonitorAnn(final JoinPoint joinPoint) {
        final MethodSignature methodSignature = (MethodSignature)joinPoint.getSignature();
        final Method method = methodSignature.getMethod();
        FuryMonitorItem annotation = method.getAnnotation(FuryMonitorItem.class);
        if (annotation == null) {
            // 判断类上面是否有注解
            annotation = joinPoint.getTarget().getClass().getAnnotation(FuryMonitorItem.class);
        }
        return annotation;
    }

    /**
     * execution(String com.bj58.zhuanzhuan.zmonitor.demo.TestService.getUser(String,int,TestService)) 取类名.方法名(参数类型列表)，
     * 以上面的表达式作为样例得到：TestService.getUser(String,int,TestService) 因为方法可能重载，这里带上了参数类型
     *
     * @return 类名.方法名(参数类型列表)字符串
     */
    private static String getMethodSignature(final JoinPoint joinPoint) {
        final String express = joinPoint.toString();
        final Matcher matcher = compile.matcher(express);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return express;
    }

}
