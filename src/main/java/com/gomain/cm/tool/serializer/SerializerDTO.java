package com.gomain.cm.tool.serializer;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

import java.util.Map;

/**
 * @author caimeng
 * @date 2023/11/13 11:39
 */
@Data
public class SerializerDTO {
    /** 印章制章人信息 **/
    @JsonDeserialize(using = MapDeserialize.class)
    private Map certMakerInfo;
    /**
     * 签章或者签名类型
     */
    @JsonSerialize(using = SignTypeSerializer.class)
    @JsonDeserialize(using = SignTypeDeserialize.class)
    private Integer signType;
    /**
     * 验证结果，成功返回0，失败非0； <br/>
     * 如果选择不验证，返回0
     */
    @JsonSerialize(using = String2HexSerializer.class)
    private String verifyResult;
}
