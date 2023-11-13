package com.gomain.cm.tool.async;

import org.slf4j.MDC;
import org.springframework.aop.interceptor.AsyncExecutionAspectSupport;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 线程池配置
 * 1. 需要在application中开启异步 @EnableAsync
 * 2.
 *
 * @author caimeng
 * @date 2022/7/28 11:40
 */
@EnableAsync
@Configuration
public class MyThreadPoolConfig {
    @Value("${thread.pool.max.size:200}")
    private int maximumPoolSize;

    /**
     * 默认线程池
     * DEFAULT_TASK_EXECUTOR_BEAN_NAME 是注解 @Async 默认查找的线程池名
     *
     * @return 线程池配置
     */
    @Bean(name = AsyncExecutionAspectSupport.DEFAULT_TASK_EXECUTOR_BEAN_NAME)
    public Executor defaultExecutor() {
        return createExecutor("taskExecutor-", maximumPoolSize, 500, 60, false, null);
    }

    /**
     * 自定义线程池
     * @param threadNamePrefix 线程名前缀
     * @param maxPoolSize 最大线程数
     * @param queueCapacity 队列容量
     * @param keepAliveSeconds 活跃时间
     * @param mdc 是否拷贝MDC参数
     * @param executor 自定义线程池
     * @return 线程池
     */
    public static ThreadPoolTaskExecutor createExecutor(
            String threadNamePrefix, int maxPoolSize, int queueCapacity,
            int keepAliveSeconds, boolean mdc, ThreadPoolTaskExecutor executor){
        if (Objects.isNull(executor)){
            executor = new ThreadPoolTaskExecutor();
        }
        //核心线程池大小
        int cpuSize = Math.max(4, Runtime.getRuntime().availableProcessors());
        executor.setCorePoolSize(cpuSize * 2);
        //最大线程数
        executor.setMaxPoolSize(maxPoolSize);
        //队列容量
        executor.setQueueCapacity(queueCapacity);
        //活跃时间
        executor.setKeepAliveSeconds(keepAliveSeconds);
        //线程名字前缀
        executor.setThreadNamePrefix(threadNamePrefix);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        // 设置日志链
        // 如果有sleuth可以不用 mdc
        if (mdc){
            executor.setTaskDecorator(runnable -> {
                Map<String, String> copyOfContextMap = MDC.getCopyOfContextMap();
                return () -> {
                    try {
                        if (Objects.nonNull(copyOfContextMap)) {
                            MDC.setContextMap(copyOfContextMap);
                        }
                        runnable.run();
                    } finally {
                        MDC.clear();
                    }
                };
            });
        }
        return executor;
    }
}
