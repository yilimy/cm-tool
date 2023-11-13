package com.gomain.cm.tool.serializer;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.util.ObjectUtils;

import java.io.Serializable;

/**
 * 不打印null的toString样式
 *
 * @author caimeng
 * @date 2022/7/22 11:10
 */
@Slf4j
public class MyNoNullStyle extends ToStringStyle implements Serializable {
    private static final long serialVersionUID = 2347542971151578670L;
    /**
     * 不打印的属性
     */
    private String[] exclude;
    /**
     * 限制长度的属性
     */
    private String[] limit;
    /**
     * 限制的长度值
     */
    private int limitLength = 100;

    public MyNoNullStyle() {
        super();
        this.setUseShortClassName(true);
    }

    /**
     * 设置需要忽略打印的属性
     * @param exclude 忽略列表
     * @return this
     */
    public MyNoNullStyle setExclude(String... exclude) {
        this.exclude = exclude;
        return this;
    }

    /**
     * 设置需要限制打印的属性
     * @param limit 限制的属性
     * @return this
     */
    public MyNoNullStyle setLimit(String... limit) {
        this.limit = limit;
        return this;
    }

    /**
     * 设置需要限制打印的长度
     * @param limitLength 限制长度，默认100
     * @return this
     */
    public MyNoNullStyle setLimitLength(int limitLength) {
        this.limitLength = limitLength;
        return this;
    }

    @Override
    public void append(StringBuffer buffer, String fieldName, Object value, Boolean fullDetail) {
        try {
            if (!ObjectUtils.isEmpty(exclude)) {
                for (String es : exclude) {
                    if (fieldName.equalsIgnoreCase(es)) {
                        return;
                    }
                }
            }
            if (value != null) {
                if (!ObjectUtils.isEmpty(limit)) {
                    for (String ls : limit) {
                        if (fieldName.equalsIgnoreCase(ls)) {
                            value = StrUtil.brief(value.toString(), limitLength);
                        }
                    }
                }
                super.append(buffer, fieldName, value, fullDetail);
            }
        } catch (Exception e) {
            log.error("NoNullStyle判断异常", e);
        }

    }
}
