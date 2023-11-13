package com.gomain.cm.tool.serializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Map;

/**
 * 含有双引号的map反序列化
 * @author caimeng
 * @date 2022/6/15 18:32
 */
public class MapDeserialize extends JsonDeserializer<Map<String, Object>> {
    @Override
    public Map<String, Object> deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException{
        TypeReference<Map<String, Object>> reference = new TypeReference<Map<String, Object>>() {};
        return new ObjectMapper().readValue(jsonParser, reference);
    }
}
