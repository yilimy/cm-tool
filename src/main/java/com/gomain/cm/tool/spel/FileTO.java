package com.gomain.cm.tool.spel;

import lombok.Data;

/**
 * 文件信息
 * @author caimeng
 * @date 2023/12/20 10:12
 */
@Data
public class FileTO {
    /**
     * 文件名
     */
    private String fileName;
    /**
     * 文件路径
     */
    private String filePath;
    /**
     * 文件类型
     */
    private String fileType;
}
