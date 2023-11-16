package com.gomain.cm.tool;

import cn.hutool.core.io.FileUtil;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * @author caimeng
 * @date 2023/11/16 11:39
 */
public class Base64Test {

    @Test
    public void urlBaseTest(){
        String path = "C:\\Users\\EDY\\Desktop\\新建文本文档(1).txt";
        String data = FileUtil.readString(path, StandardCharsets.UTF_8);
        byte[] decode = Base64.getUrlDecoder().decode(data);
        FileUtil.writeBytes(decode, "C:\\Users\\EDY\\Desktop\\decode.txt");
    }
}
