package com.gomain.cm.tool;

import cn.hutool.core.io.FileUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.Optional;
import java.util.Set;

/**
 * 接口地址
 * @author caimeng
 * @date 2024/3/11 17:40
 */
@Slf4j
public class MappingsTest {

    @Test
    public void mappingsURITest() {
        String filePath = "E:\\Documents\\A_工作日志\\G广东政府侧\\api所有接口.json";
        byte[] bytes = FileUtil.readBytes(filePath);
        Set<String> keySet = Optional.of(bytes)
                .map(String::new)
                .map(JSON::parseObject)
                .orElse(new JSONObject())
                .keySet();
        log.info("keySet = {}", keySet);

    }
}
