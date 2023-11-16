package com.gomain.cm.tool.serializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.springframework.util.ObjectUtils;

import java.io.IOException;

/**
 * @author caimeng
 * @date 2022/6/15 18:10
 */
public class SignTypeDeserialize extends JsonDeserializer<Integer> {

    @Override
    public Integer deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException{
        return parseSignType(jsonParser.getText());
    }

    /**
     * 将string型签名或签章类型反序列化为Int
     * @param string 签章或签名类型描述
     * @return int
     */
    public static Integer parseSignType(String string) {
        int describe = SignTypeSerializer.TYPE_UNKNOWN;
        if (ObjectUtils.isEmpty(string)) {
            return describe;
        }
        switch (string) {
            case SignTypeSerializer.GBT38540 : return SignTypeSerializer.TYPE_GBT38540;
            case SignTypeSerializer.GBT35275 : return SignTypeSerializer.TYPE_GBT35275;
            case SignTypeSerializer.GMT0031 : return SignTypeSerializer.TYPE_GMT0031;
            case SignTypeSerializer.PKCS7 : return SignTypeSerializer.TYPE_PKCS;
            default : return SignTypeSerializer.TYPE_UNKNOWN;
        }
    }
}
