package com.gomain.cm.tool.feign.hystrix;

import com.gomain.cm.tool.feign.GdApiFeign;
import com.gomain.cm.tool.feign.bean.DynamicSign;
import com.gomain.cm.tool.feign.bean.FormatSignRsp;
import com.gomain.cm.tool.pojo.Result;
import feign.Response;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author caimeng
 * @date 2024/2/23 11:32
 */
@Slf4j
@Component
public class GdApiFeignFallBackFactory implements FallbackFactory<GdApiFeign> {
    @Override
    public GdApiFeign create(Throwable cause) {
        return new GdApiFeign() {
            @Override
            public Result<FormatSignRsp> dynamicSign(DynamicSign dynamicSign) {
                log.error("feign:请求广东省api签章接口失败, param={}", dynamicSign);
                log.error("签章失败", cause);
                return Result.fail(-1, "请求广东省api签章接口失败");
            }

            @Override
            public Response downFile(String documentPath) {
                String errorMsg = "feign:下载文件失败";
                log.error("{}, documentPath={}", errorMsg, documentPath);
                log.error(errorMsg, cause);
                return null;
            }
        };
    }
}
