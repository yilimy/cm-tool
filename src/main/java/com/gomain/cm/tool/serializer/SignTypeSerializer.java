package com.gomain.cm.tool.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

/**
 * 国脉板式库的签章类型映射类
 * <p>
 * 印章类型也使用此映射(尚未确定)
 * <p>
 * 分组：序列化
 *
 * @author caimeng
 * @date 2021/5/19 15:22
 */
public class SignTypeSerializer extends JsonSerializer<Integer> {
    /**
     * 1-GB/T 38540
     */
    public static final int TYPE_GBT38540 = 1;
    /**
     * 2-GB/T35275
     */
    public static final int TYPE_GBT35275 = 2;
    /**
     * 3-GM/T 0031
     */
    public static final int TYPE_GMT0031 = 3;
    /**
     * 4-TYPE_PKCS
     */
    public static final int TYPE_PKCS = 4;
    /**
     * 7-rsa
     */
    public static final int TYPE_RSA = 7;
    /**
     * 5，unknow
     */
    public static final int TYPE_UNKNOWN = 5;
    /**
     * [es.TYPE_GBT38540  GB/T 38540标准]
     */
    public static final String GBT38540 = "es.TYPE_GBT38540";
    /**
     * [es.TYPE_GMT0031   GM/T 0031标准]
     */
    public static final String GMT0031 = "es.TYPE_GMT0031";
    /**
     * [ds.TYPE_GBT35275  GB/T 35275标准]
     */
    public static final String GBT35275 = "ds.TYPE_GBT35275";
    /**
     * [ds.PKCS7     TYPE_PKCS#7数字签名 pkcs7-RSA]
     */
    public static final String PKCS7 = "ds.PKCS7";
    /**
     * [unknown      未识别的签名类型]
     */
    public static final String UNKNOWN = "unknown";

    /**
     * 将int型签名或签章类型转换成文本
     * @param integer 签章或签名类型
     * @return 文本描述
     */
    public static String parseSignType(Integer integer) {
        String describe = UNKNOWN;
        if (integer == null) {
            return UNKNOWN;
        }
        switch (integer) {
            case TYPE_GBT38540:
                describe = GBT38540;
                break;
            case TYPE_GBT35275:
                describe = GBT35275;
                break;
            case TYPE_GMT0031:
                describe = GMT0031;
                break;
            case TYPE_PKCS:
                describe = PKCS7;
                break;
            case TYPE_UNKNOWN:
                describe = UNKNOWN;
                break;
            default:
                break;
        }
        return describe;
    }

    @Override
    public void serialize(Integer integer, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeObject(parseSignType(integer));
    }
}
