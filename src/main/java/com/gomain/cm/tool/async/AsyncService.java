package com.gomain.cm.tool.async;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncExecutionAspectSupport;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * 包含有异步任务的服务
 * @author caimeng
 * @date 2023/11/13 10:28
 */
@Slf4j
@Service
public class AsyncService {

    /**
     * 理论上默认的线程池是taskExecutor，但是有时候不灵，需要显示指定。
     * 线程池配置:{@link com.gomain.cm.tool.async.MyThreadPoolConfig}
     */
    @Async(AsyncExecutionAspectSupport.DEFAULT_TASK_EXECUTOR_BEAN_NAME)
    public void asyncDeal() {
        log.info("执行异步任务");
    }
}
