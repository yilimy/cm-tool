package com.gomain.cm.tool.async;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;

/**
 * 含有ThreadLocal业务的线程池配置
 * @author caimeng
 * @date 2023/11/13 10:17
 */
@EnableAsync
@Configuration
public class MyThreadPoolWithLocalConfig {
    @Value("${thread.pool.max.size:200}")
    private int maximumPoolSize;
    /** 文件和印章的保存对象 **/
    public static final ThreadLocal<ThreadLogPO> LOG_LOCAL = new ThreadLocal<>();

    /**
     * 一件事项目定制化线程池
     * @return 线程池
     */
    @Bean(name = "yjsExecutor")
    public Executor yjsExecutor() {
        return MyThreadPoolConfig.createExecutor("yjsTask-", maximumPoolSize, 500, 60, false, new YjsThreadPoolTaskExecutor());
    }

    /**
     * 当拒绝策略是CallerRunsPolicy时，执行任务的线程是父线程，这个时候清理数据，会直接把父线程的threadLocal清理，极有可能导致后续业务出错，
     * 所以我们需要判断一下，当前执行任务的线程是不是和父线程相等，
     * 如果是的话，就没必要执行移除threadlocal，让父线程的逻辑代码自行移除
     * 如果只是针对 @Async 注解使用，下面代码中，没必要重写execute方法，直接重写submit就行
     * <a href="https://blog.csdn.net/yang11qiang/article/details/125490779">解决@Async导致ThreadLocal值丢失问题</>
     */
    private static class YjsThreadPoolTaskExecutor extends ThreadPoolTaskExecutor {
        @SuppressWarnings("NullableProblems")
        @Override
        public Future<?> submit(Runnable task) {
            ThreadLogPO logPO = LOG_LOCAL.get();
            return super.submit(new Runner(Thread.currentThread(), task, logPO));
        }

        @SuppressWarnings("NullableProblems")
        @Override
        public <T> Future<T> submit(Callable<T> task) {
            ThreadLogPO logPO = LOG_LOCAL.get();
            return super.submit(new Caller<>(Thread.currentThread(), task, logPO));
        }
    }

    /**
     * 自定义Runner
     * 清理ThreadLocal数据
     */
    @Slf4j
    private static class Runner implements Runnable{
        private final Thread parentThread;
        private final Runnable runnable;
        private final ThreadLogPO logPO;

        Runner(Thread parentThread, Runnable runnable, ThreadLogPO logPO) {
            this.parentThread = parentThread;
            this.runnable = runnable;
            this.logPO = logPO;
        }

        @Override
        public void run() {
            // 这里是子线程
            LOG_LOCAL.set(logPO);
            try {
                runnable.run();
            } catch (Exception e){
                log.error("异步任务异常:{}", e.getMessage());
                throw e;
            } finally {
                if (parentThread != Thread.currentThread()){
                    LOG_LOCAL.remove();
                }
            }
        }
    }

    /**
     * 自定义Caller
     * 清理ThreadLocal数据
     */
    @Slf4j
    private static class Caller<V> implements Callable<V>{
        private final Thread parentThread;
        private final Callable<V> callable;
        private final ThreadLogPO logPO;

        Caller(Thread parentThread, Callable<V> callable, ThreadLogPO logPO) {
            this.parentThread = parentThread;
            this.callable = callable;
            this.logPO = logPO;
        }

        @Override
        public V call() throws Exception {
            // 这里是子线程
            LOG_LOCAL.set(logPO);
            try {
                return callable.call();
            } catch (Exception e){
                log.error("异步任务异常:{}", e.getMessage());
                throw e;
            } finally {
                if (parentThread != Thread.currentThread()){
                    LOG_LOCAL.remove();
                }
            }
        }
    }
}
