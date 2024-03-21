package com.gomain.cm.tool.feign;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.SM3;
import cn.hutool.extra.spring.SpringUtil;
import feign.Contract;
import feign.MethodMetadata;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ObjectUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 读取配置文件中的 {gd.appKey}, {gd.appSecret} 并设置网关认证的请求头
 * <p>
 *     使用该配置后，http解析使用feign-core的解析方式，而不是springMVC；
 *     注解使用 @RequestLine，替换spring的 @RequestMapping
 * @author caimeng
 * @date 2024/2/26 13:49
 */
@Slf4j
public class GdContract extends Contract.Default{

    private static String appKey;
    private static String secret;

    private static final List<String> AUTH_HEADER_KEYS = Arrays.asList("appkey", "random", "digest");

    @Override
    protected void processAnnotationOnClass(MethodMetadata data, Class<?> targetType) {
        super.processAnnotationOnClass(data, targetType);
        if (StrUtil.hasBlank(appKey, secret)) {
            initAuthData();
        }
        Map<String, Collection<String>> resHeader = data.template().headers();
        Map<String, Collection<String>> desHeader = new HashMap<>();
        resHeader.forEach((k, v) -> {
            if (!AUTH_HEADER_KEYS.contains(k)) {
                desHeader.put(k ,v);
            }
        });
        // 设置网关认证参数
        String random = RandomUtil.randomNumbers(10);
        String plain = appKey + secret + random;
        String digest = SM3.create().digestHex(plain);
        desHeader.put("appkey", Collections.singleton(appKey));
        desHeader.put("random", Collections.singleton(random));
        desHeader.put("digest", Collections.singleton(digest));
        data.template().headers(desHeader);
    }

    /**
     * 初始化认证参数
     */
    private void initAuthData() {
        // 需要 hutool-all:5.8.10
        appKey = SpringUtil.getProperty("gd.appKey");
        if (ObjectUtils.isEmpty(appKey)) {
            throw new RuntimeException("配置文件中读取gd.appKey失败");
        }
        secret = SpringUtil.getProperty("gd.appSecret");
        if (ObjectUtils.isEmpty(secret)) {
            throw new RuntimeException("配置文件中读取gd.appSecret失败");
        }
    }
}
