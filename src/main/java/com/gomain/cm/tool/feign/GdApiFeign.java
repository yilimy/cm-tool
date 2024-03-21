package com.gomain.cm.tool.feign;

import com.gomain.cm.tool.feign.bean.DynamicSign;
import com.gomain.cm.tool.feign.bean.FormatSignRsp;
import com.gomain.cm.tool.feign.hystrix.GdApiFeignFallBackFactory;
import com.gomain.cm.tool.pojo.Result;
import feign.Param;
import feign.RequestLine;
import feign.Response;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 远程调用广东省签章接口
 * @author caimeng
 * @date 2024/2/23 11:28
 */
@FeignClient(
        value = "GD-API",
        // 广东省网关地址
        url = "${gd.host:http://192.168.200.143:8781}",
        configuration = GdContract.class,
        fallbackFactory = GdApiFeignFallBackFactory.class)
public interface GdApiFeign {

    /**
     * 广东省api签章服务
     * @param dynamicSign 签章参数
     * @return 文件路径
     */
    @RequestLine("POST /core/dynamicSign")
//    @RequestMapping(value = "/core/dynamicSign", method = RequestMethod.POST)
    Result<FormatSignRsp> dynamicSign(@RequestBody DynamicSign dynamicSign);
    /**
     * 下载文件 <br>
     * 该接口调用成功时，返回文件流 <br>
     * @param documentPath String
     * @return <p>
     *     成功时返回文件流，
     *     失败时返回BWJsonResult&lt;?&gt;
     * @author zhaojy
     * @date 2018年9月5日16:40:06
     */
    @RequestLine("POST /core/downFile?documentPath={documentPath}")
//    @RequestMapping(path = "/core/downFile", method = RequestMethod.POST)
    Response downFile(@Param(value = "documentPath") String documentPath);

}
