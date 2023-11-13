package com.gomain.cm.tool.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * 序列化类，
 * 字符串转换为十六进制错误码
 *
 * @author caimeng
 * @date 2022/3/19 18:20
 */
@Slf4j
public class String2HexSerializer extends JsonSerializer<String> {

    @Override
    public void serialize(String string, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeObject(parseHex(string));
    }

    /**
     * 将字符串型的错误码转换成十六进制
     * @param string 待转换字符串
     * @return 十六进制字符串
     */
    public static String parseHex(String string) {
        try {
            return Integer.toHexString(Integer.parseInt(string));
        } catch (Exception e){
            log.error("不能转换成十六进制的错误码：[{}]", string);
        }
        return string;
    }
}
