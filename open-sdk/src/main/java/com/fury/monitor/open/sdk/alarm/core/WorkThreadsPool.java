package com.okcoin.cloud.client.demo.alarm.core;


import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

import io.micrometer.core.instrument.util.NamedThreadFactory;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

import static com.okcoin.cloud.client.demo.alarm.core.CoreConstants.LOG_LIMIT_TIME;
import static com.okcoin.cloud.client.demo.alarm.core.CoreConstants.QUEUE_BLOCK_PERCENT;

/**
 * 线程池的线程数如何估算： 根据 thread_pool_active_count 这个监控项的值预估
 * 
 * @author : fury
 * @date : 2020/8/7
 */
@Slf4j
@Component
public class WorkThreadsPool {

    private static final Map<String, ThreadPoolExecutor> namePoolMap = new HashMap<>();

    public static ThreadPoolExecutor getThreadPoolExecutor(final int corePoolSize, final int maximumPoolSize,
        final int queueSize, final String monitorKey) {
        final BlockingQueue<Runnable> workQueue =
            queueSize == 0 ? new SynchronousQueue<>() : new ArrayBlockingQueue<>(queueSize);
        final ThreadFactory threadFactory = genThreadFactory(monitorKey);
        final RejectedExecutionHandler policy = genDiscardOldestPolicy(monitorKey);
        return getThreadPoolExecutor(corePoolSize, maximumPoolSize, 0, workQueue, threadFactory, policy, monitorKey);
    }

    public static ThreadPoolExecutor getThreadPoolExecutor(final int corePoolSize, final int maximumPoolSize,
        final int queueSize, final RejectedExecutionHandler policy, final String monitorKey) {
        final BlockingQueue<Runnable> workQueue =
            queueSize == 0 ? new SynchronousQueue<>() : new ArrayBlockingQueue<>(queueSize);
        final ThreadFactory threadFactory = genThreadFactory(monitorKey);
        return getThreadPoolExecutor(corePoolSize, maximumPoolSize, 0, workQueue, threadFactory, policy, monitorKey);
    }

    public static ThreadPoolExecutor getThreadPoolExecutor(final int corePoolSize, final int maximumPoolSize,
        final int keepaliveSeconds, final int queueSize, final String monitorKey) {
        final BlockingQueue<Runnable> workQueue =
            queueSize == 0 ? new SynchronousQueue<>() : new ArrayBlockingQueue<>(queueSize);
        final ThreadFactory threadFactory = genThreadFactory(monitorKey);
        final RejectedExecutionHandler policy = genDiscardOldestPolicy(monitorKey);
        return getThreadPoolExecutor(corePoolSize, maximumPoolSize, keepaliveSeconds, workQueue, threadFactory, policy,
            monitorKey);
    }

    public static ThreadPoolExecutor getThreadPoolExecutor(final int corePoolSize, final int maximumPoolSize,
        final int keepaliveSeconds, final int queueSize, final RejectedExecutionHandler policy,
        final String monitorKey) {
        final BlockingQueue<Runnable> workQueue =
            queueSize == 0 ? new SynchronousQueue<>() : new ArrayBlockingQueue<>(queueSize);
        final ThreadFactory threadFactory = genThreadFactory(monitorKey);
        return getThreadPoolExecutor(corePoolSize, maximumPoolSize, keepaliveSeconds, workQueue, threadFactory, policy,
            monitorKey);
    }

    public static ThreadPoolExecutor getThreadPoolExecutor(final int corePoolSize, final int maximumPoolSize,
        final int keepaliveSeconds, final BlockingQueue<Runnable> workQueue, final RejectedExecutionHandler policy,
        final String monitorKey) {
        final ThreadFactory threadFactory = genThreadFactory(monitorKey);
        return getThreadPoolExecutor(corePoolSize, maximumPoolSize, keepaliveSeconds, workQueue, threadFactory, policy,
            monitorKey);
    }

    public static ThreadPoolExecutor getThreadPoolExecutor(final int corePoolSize, final int maximumPoolSize,
        final int keepaliveSeconds, final BlockingQueue<Runnable> workQueue, final ThreadFactory threadFactory,
        RejectedExecutionHandler policy, final String monitorKey) {

        if (namePoolMap.containsKey(monitorKey)) {
            throw new IllegalStateException(monitorKey + "线程池已经存在");
        }
        if (policy == null) {
            throw new NullPointerException("线程池RejectedExecutionHandler为空");
        }
        if (!(policy instanceof MonitorRejectedExecutionHandler)) {
            policy = new MonitorRejectedExecutionHandler(policy, monitorKey);
        }

        final ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(corePoolSize, maximumPoolSize,
            keepaliveSeconds, TimeUnit.SECONDS, workQueue, threadFactory, policy) {

            private final ThreadLocal<Long> taskExecuteStart = new ThreadLocal<>();

            @Override
            protected void beforeExecute(final Thread t, final Runnable r) {
                // 活跃线程数
                final int activeCount = this.getActiveCount();
                FuryMonitor.max(LocalInfo.clusterName + "_thread_pool_active_count", activeCount, monitorKey);
                final int blockTaskCount = this.getQueue().size();
                taskExecuteStart.set(System.currentTimeMillis());

                // 排队任务超过最大线程数的10% 认为队列拥堵
                if (blockTaskCount > maximumPoolSize / QUEUE_BLOCK_PERCENT) {
                    // 线程队列任务堆积
                    log.warn("[ARCH_ZMONITOR_CLIENT_thread_pool_stat]{}_Before_Slow", monitorKey);
                    FuryMonitor.max(LocalInfo.clusterName + "_thread_pool_queue_size", blockTaskCount, monitorKey);
                }

                if (log.isDebugEnabled()) {
                    log.debug(
                        "[ARCH_ZMONITOR_CLIENT_thread_pool_stat]threadPoolName:{}, Before: ActiveCount[{}], TaskCount[{}], complete[{}], QueueSize[{}],Executor:{}",
                        monitorKey, activeCount, this.getTaskCount(), this.getCompletedTaskCount(), blockTaskCount,
                        super.toString());
                }
            }

            @Override
            protected void afterExecute(final Runnable r, final Throwable t) {
                super.afterExecute(r, t);
                final Long start = taskExecuteStart.get();
                final long executeTime = System.currentTimeMillis() - start;
                if (executeTime > LOG_LIMIT_TIME) {
                    log.warn("[ARCH_ZMONITOR_CLIENT_thread_pool_stat]Task name:{} execute ms: {}", monitorKey,
                        executeTime);
                    FuryMonitor.max(LocalInfo.clusterName + "_thread_pool_execute_time", (int)executeTime, monitorKey);
                }
                taskExecuteStart.remove();
            }

            @Override
            protected void terminated() {
                log.info("[ARCH_ZMONITOR_CLIENT_thread_pool_stat]{}_Terminated", monitorKey);
            }
        };
        threadPoolExecutor.prestartCoreThread();
        namePoolMap.put(monitorKey, threadPoolExecutor);
        return threadPoolExecutor;
    }

    /**
     * 更改初始化好的线程池的参数这些方法，业务方可以直接设置
     */
    public static void setCorePoolSize(final String threadPoolName, final int corePoolSize) {
        final ThreadPoolExecutor threadPoolExecutor = namePoolMap.get(threadPoolName);
        if (threadPoolExecutor != null) {
            threadPoolExecutor.setCorePoolSize(corePoolSize);
        }
    }

    public static void setMaximumPoolSize(final String threadPoolName, final int maximumPoolSize) {
        final ThreadPoolExecutor threadPoolExecutor = namePoolMap.get(threadPoolName);
        if (threadPoolExecutor != null) {
            threadPoolExecutor.setMaximumPoolSize(maximumPoolSize);
        }
    }

    public static void closeAllThreadPool() {
        namePoolMap.values().forEach(ThreadPoolExecutor::shutdown);
    }

    public static RejectedExecutionHandler genDiscardOldestPolicy(final String monitorKey) {
        return new MonitorRejectedExecutionHandler(new ThreadPoolExecutor.DiscardOldestPolicy(), monitorKey);
    }

    private static class MonitorRejectedExecutionHandler implements RejectedExecutionHandler {
        private final RejectedExecutionHandler handler;
        private final String monitorKey;

        public MonitorRejectedExecutionHandler(final RejectedExecutionHandler handler, final String monitorKey) {
            this.handler = handler;
            this.monitorKey = monitorKey;
        }

        @Override
        public void rejectedExecution(final Runnable r, final ThreadPoolExecutor e) {
            log.error(
                "[ARCH_ZMONITOR_CLIENT_Task_Rejected_Execution]threadPoolName:{}, poolSize: {} ,activeCount: {}, corePoolSize: {}, maxPoolSize: {}, largestPoolSize: {},"
                    + "taskCount: {} (completed: {}),Executor status:(isShutdown:{}, isTerminated:{}, isTerminating:{})",
                monitorKey, e.getPoolSize(), e.getActiveCount(), e.getCorePoolSize(), e.getMaximumPoolSize(),
                e.getLargestPoolSize(), e.getTaskCount(), e.getCompletedTaskCount(), e.isShutdown(), e.isTerminated(),
                e.isTerminating());
            FuryMonitor.sum(LocalInfo.clusterName + "_thread_pool_rejected", 1, monitorKey);
            handler.rejectedExecution(r, e);
        }
    }

    private static ThreadFactory genThreadFactory(final String monitorKey) {
        return new NamedThreadFactory(monitorKey);
    }
}
