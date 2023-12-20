package com.gomain.cm.tool.spel;

import cn.hutool.core.date.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 测试SpEL表达式的控制器
 * @author caimeng
 * @date 2023/12/20 10:09
 */
@Slf4j
@RestController
@RequestMapping(("/spEL"))
public class SpELController {

    @SpELAnnotation(fileName = "#fileTO.fileName")
    @PostMapping("/fileInfo")
    public String fileInfo(@RequestBody FileTO fileTO){
        log.info("fileTO={}", fileTO);
        return DateUtil.now();
    }
}
