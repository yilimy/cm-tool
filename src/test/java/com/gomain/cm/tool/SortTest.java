package com.gomain.cm.tool;

import cn.hutool.core.io.FileUtil;
import org.junit.Test;
import org.springframework.util.ObjectUtils;

import java.nio.charset.Charset;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author caimeng
 * @date 2024/4/7 17:19
 */
public class SortTest {

    @Test
    public void sortTest() {
        String sourcePath = "E:\\Documents\\A_工作日志\\y烟草\\预生产印章和印模地址.txt";
        String desPath = "C:\\Users\\EDY\\Desktop\\seal_path.txt";
        List<String> strings = FileUtil.readLines(sourcePath, Charset.defaultCharset());
        List<String> resultList = strings.stream()
                .filter(str -> !ObjectUtils.isEmpty(str))
                .map(str -> str.substring(0, str.lastIndexOf("/")))
                .distinct()
                .sorted().collect(Collectors.toList());
        FileUtil.writeLines(resultList, desPath, Charset.defaultCharset());
    }

    @Test
    public void filePathTest() {
        String filePath = "/../../../etc/shadow";
        String normalize = FileUtil.normalize(filePath);
        System.out.println(normalize);
    }
}
