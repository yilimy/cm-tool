package com.gomain.cm.tool.hutool;

import cn.hutool.system.HostInfo;
import cn.hutool.system.JavaInfo;
import cn.hutool.system.JavaRuntimeInfo;
import cn.hutool.system.JavaSpecInfo;
import cn.hutool.system.JvmInfo;
import cn.hutool.system.JvmSpecInfo;
import cn.hutool.system.OsInfo;
import cn.hutool.system.RuntimeInfo;
import cn.hutool.system.SystemUtil;
import cn.hutool.system.UserInfo;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

/**
 * @author caimeng
 * @date 2024/4/7 18:34
 */
@Slf4j
public class SystemTest {

    /**
     * 测试：打印系统信息
     */
    @Test
    public void printSystemInfosTest() {
        JvmSpecInfo jvmSpecInfo = SystemUtil.getJvmSpecInfo();
        log.info("jvmSpecInfo={}", jvmSpecInfo);
        JvmInfo jvmInfo = SystemUtil.getJvmInfo();
        log.info("jvmInfo={}", jvmInfo);
        JavaSpecInfo javaSpecInfo = SystemUtil.getJavaSpecInfo();
        log.info("javaSpecInfo={}", javaSpecInfo);
        JavaInfo javaInfo = SystemUtil.getJavaInfo();
        log.info("javaInfo={}", javaInfo);
        JavaRuntimeInfo javaRuntimeInfo = SystemUtil.getJavaRuntimeInfo();
        log.info("javaRuntimeInfo={}", javaRuntimeInfo);
        OsInfo osInfo = SystemUtil.getOsInfo();
        log.info("osInfo={}", osInfo);
        UserInfo userInfo = SystemUtil.getUserInfo();
        log.info("userInfo={}", userInfo);
        HostInfo hostInfo = SystemUtil.getHostInfo();
        log.info("hostInfo={}", hostInfo);
        RuntimeInfo runtimeInfo = SystemUtil.getRuntimeInfo();
        log.info("runtimeInfo={}", runtimeInfo);
    }
}
