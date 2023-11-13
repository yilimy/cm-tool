package com.gomain.cm.tool.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 定时任务
 * @author caimeng
 * @date 2023/11/13 10:39
 */
@Slf4j
@Component
@ConditionalOnExpression("${shanxi.enable:false}")
public class SxTask {
    private final TaskService taskService;

    public SxTask(@Autowired TaskService taskService) {
        this.taskService = taskService;
    }

    /**
     * 定时任务: 每5秒执行一次
     */
    @Scheduled(cron = "${shanxi.task.cron:*/5 * * * * ?}")
    public void scheduledDo() {
        log.info("执行定时任务");
        taskService.scheduledDo();
    }
}
