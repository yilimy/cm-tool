package com.gomain.cm.tool.feign.bean;

import lombok.Data;

/**
 * format签章返回数据
 * @author caimeng
 * @date 2023/10/11 18:25
 */
@Data
public class FormatSignRsp {
    /**
     * 签章后文件路径
     */
    private String layoutPath;
}
