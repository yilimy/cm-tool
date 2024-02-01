package com.gomain.cm.tool.forward;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.SM3;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gomain.cm.tool.pojo.Result;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.springframework.http.HttpMethod;
import org.springframework.util.ObjectUtils;
import org.springframework.web.servlet.HandlerExceptionResolver;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * 转发到省枢纽的过滤器
 * @author caimeng
 * @date 2024/1/31 10:20
 */
@Slf4j
public class ForwardHubFilter implements Filter {
    /**
     * 异常处理器，其实主要是以下两个实现 <br>
     * {@link org.springframework.boot.web.servlet.error.DefaultErrorAttributes DefaultErrorAttributes}
     * {@link org.springframework.boot.actuate.autoconfigure.web.servlet.CompositeHandlerExceptionResolver CompositeHandlerExceptionResolver}
     * 也主要针对这两个实现做异常捕获和结果处理
     */
    private final HandlerExceptionResolver exceptionResolver;
    /**
     * 自定义异常处理器
     */
    private final ForwardHubResolver forwardHubResolver;
    /** 请求超时时间 **/
    @Setter
    private int connectionRequestTimeout = -1;
    /** 连接超时时间 **/
    @Setter
    private int connectTimeout = -1;
    /** socket超时时间 **/
    @Setter
    private int socketTimeout = -1;
    /** 省枢纽请求地址 **/
    @Setter
    private static String hubHost;
    /** 省枢纽appKey **/
    @Setter
    private static String targetAppKey;
    /** 省枢纽appSecret **/
    @Setter
    private static String targetAppSecret;
    @Setter
    private List<String> headerExclude = Arrays.asList(
            "content-length", "access-token", "appkey", "random", "digest");

    public ForwardHubFilter(HandlerExceptionResolver exceptionResolver,
                            ForwardHubResolver forwardHubResolver) {
        this.exceptionResolver = exceptionResolver;
        this.forwardHubResolver = forwardHubResolver;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        try {
            forwardFilter(request, response);
        } catch (Exception e) {
            log.error("转发服务异常", e);
            exceptionResolver.resolveException(request, response, forwardHubResolver, e);
        }
    }

    /**
     * 开启一个http请求，执行转发
     * @param request 原请求
     * @param response 原响应
     */
    @SneakyThrows({Exception.class})
    private void forwardFilter(HttpServletRequest request, HttpServletResponse response) {
        HttpRequestBase httpRequestBase;
        // 此处为 application/x-www-form-urlencoded
        String query = request.getQueryString();
        // 获取请求路径
        String uri = request.getRequestURI();
        beforeForward();
        // 1. 请求地址
        String newUrl = hubHost + uri + Optional.ofNullable(query).filter(s -> s.length() > 0).map(s -> "?" + s).orElse("");
        log.info("接口转发：newUrl={}", newUrl);
        // 2. 设置请求方式
        String method = request.getMethod();
        log.info("请求方式：method={}", method);
        if (HttpMethod.GET.name().equalsIgnoreCase(method)) {
            httpRequestBase = new HttpGet(newUrl);
        } else if (HttpMethod.POST.name().equalsIgnoreCase(method)) {
            httpRequestBase = new HttpPost(newUrl);
        } else {
            throw new RuntimeException("不支持的请求类型: " + method);
        }
        // 3. 设置请求头
        copyRequestHeader(request, httpRequestBase);
        // 4. 设置POST参数
        String contentType = request.getContentType();
        log.info("contentType={}", contentType);
        if (httpRequestBase instanceof HttpPost && !ObjectUtils.isEmpty(contentType)) {
            if (contentType.startsWith(ContentType.APPLICATION_JSON.getMimeType())) {
                // application/json
                String body = IoUtil.read(request.getInputStream(), StandardCharsets.UTF_8);
                // 请求中的空格不能去，为了发现空格
                log.info("request body={}", StrUtil.brief(body, 300));
                ((HttpPost) httpRequestBase).setEntity(new StringEntity(body, ContentType.APPLICATION_JSON));
            } else if (contentType.startsWith(ContentType.MULTIPART_FORM_DATA.getMimeType())) {
                // multipart/form-data
                copyMultipartFormData(request, httpRequestBase);
            } else {
                // 剩下的使用 x-www-form-urlencoded 方案去处理，其他请求暂不做枚举处理了
                copyXWwwFormUrlencoded(request, httpRequestBase);
            }
        }
        // 5. 连接设置
        RequestConfig config = RequestConfig.custom()
                .setConnectionRequestTimeout(connectionRequestTimeout)
                .setSocketTimeout(socketTimeout)
                .setConnectTimeout(connectTimeout)
                .build();
        httpRequestBase.setConfig(config);
        // 6. 提交请求
        try (CloseableHttpClient httpClient = HttpClients.createDefault()){
            CloseableHttpResponse execute = httpClient.execute(httpRequestBase);
            // 打印结果需要读取流，尽量不要对源数据进行处理，只打印响应码
            int statusCode = execute.getStatusLine().getStatusCode();
            log.info("响应结果码：{}", statusCode);
            // 7. 返回前的处理
            HttpEntity responseEntity = execute.getEntity();
            // 8. 设置响应头
            Header[] responseHeaders = execute.getAllHeaders();
            for (Header header : responseHeaders) {
                // Content-Length 和 Content-Encoding 不能同时存在，故两个属性都不复制
                if (HTTP.CONTENT_LEN.equalsIgnoreCase(header.getName())
                        || HTTP.TRANSFER_ENCODING.equalsIgnoreCase(header.getName())) {
                    continue;
                }
                log.debug("response header name={}, value={}", header.getName(), header.getValue());
                // 复制响应头
                response.setHeader(header.getName(), header.getValue());
            }
            // 9. 转发结果写入响应体
            if (statusCode == HttpStatus.SC_NOT_FOUND){
                // 针对404单独处理
                response404(response, request.getServletPath());
            } else {
                afterResponse(response, responseEntity);
            }
        } catch (ClientProtocolException e) {
            log.error("转发失败: " + newUrl, e);
            throw new RuntimeException("转发失败, url=" + newUrl);
        }
    }

    /**
     * 转发前的准备
     */
    private void beforeForward() {
        if (StrUtil.hasBlank(hubHost, targetAppKey, targetAppSecret)) {
            hubHost = "http://192.168.200.36:7780/api";
            targetAppKey = "100331";
            targetAppSecret = "2f2a91f9be3977d7";
            log.info("hubUrl={}, targetAppKey={}, targetAppSecret={}", hubHost, targetAppKey, targetAppSecret);
        }
    }

    /**
     * 复制请求中的请求头
     * <p>
     * 1. 不能设置 Content-Length 的值，让 CloseableHttpClient 自动设置
     * 2. multipart/form-data 不能复制请求头，错误的请求头，会导致接收端文件读取不到，委托 CloseableHttpClient 自动设置
     *
     * @param request         原请求
     * @param httpRequestBase 转发请求
     */
    private void copyRequestHeader(HttpServletRequest request, HttpRequestBase httpRequestBase) {
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String key = headerNames.nextElement();
            // 不设置header长度
            if (headerExclude.contains(key)) {
                continue;
            }
            String value = request.getHeader(key);
            log.debug("request header key={}, value={}", key, value);
            httpRequestBase.setHeader(key, value);
        }
        String random = RandomUtil.randomNumbers(10);
        String plain = targetAppKey + targetAppSecret + random;
        String digest = SM3.create().digestHex(plain);
        httpRequestBase.setHeader("appkey", targetAppKey);
        httpRequestBase.setHeader("random", random);
        httpRequestBase.setHeader("digest", digest);
    }

    /**
     * 复制 POST 请求 form-data 请求方式中的请求数据
     *
     * @param request         待复制请求
     * @param httpRequestBase 转发请求
     */
    @SneakyThrows({IOException.class, ServletException.class})
    protected void copyMultipartFormData(HttpServletRequest request, HttpRequestBase httpRequestBase) {
        // 附带文件的请求，contentType后有数据描述，小数据为base64的值，大数据疑似为文件大小
        // e.g. multipart/form-data; boundary=--------------------------394758598706425280136232
        MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create()
                // 防止中文文件名导致的乱码
                .setMode(HttpMultipartMode.RFC6532);
        // multipart/form-data 时才能获取，否则报错
        Collection<Part> parts = request.getParts();
        if (!ObjectUtils.isEmpty(parts)) {
            // 数据为文件时，Content-Type 中含有长度信息 [boundary]，需要去除长度
            httpRequestBase.removeHeaders(HTTP.CONTENT_TYPE);
            for (Part part : parts){
                try {
                    InputStream is = part.getInputStream();
                    entityBuilder.addBinaryBody(
                            part.getName(),
                            is,
                            ContentType.APPLICATION_OCTET_STREAM,
                            part.getSubmittedFileName());
                } catch (Exception e) {
                    log.error("copy part failed", e);
                }
            }
        }
        ((HttpPost) httpRequestBase).setEntity(entityBuilder.build());
    }

    /**
     * 复制 POST 请求 application/x-www-form-urlencoded 请求方式中的请求数据
     *
     * @param request         待复制请求
     * @param httpRequestBase 转发请求
     */
    private void copyXWwwFormUrlencoded(HttpServletRequest request, HttpRequestBase httpRequestBase) throws Exception{
        // 该接口会将URL问号后面的参数一同获取到
        Map<String, String[]> parameterMap = request.getParameterMap();
        List<NameValuePair> params = new ArrayList<>();
//        parameterMap.forEach((k, v) -> Stream.of(v).forEach(i -> params.add(new BasicNameValuePair(k, i))));
        parameterMap.forEach((k, v) -> params.add(new BasicNameValuePair(k, String.join(",", v))));
        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(params);
        ((HttpPost) httpRequestBase).setEntity(entity);
        // 如果URL地址含有问号及参数，需要重置URI
        URI uri = httpRequestBase.getURI();
        if (Objects.nonNull(uri.getQuery())) {
            URI replaceUri = new URI(uri.getScheme(), uri.getAuthority(), uri.getPath(), null, uri.getFragment());
            httpRequestBase.setURI(replaceUri);
        }
    }

    /**
     * 针对404单独处理
     * @param response 响应体
     * @param path 请求URI
     */
    private void response404(HttpServletResponse response, String path){
        try (ServletOutputStream outputStream = response.getOutputStream()) {
            Result<?> rsp404 = Result.fail().setCode(404).setMessage("Not Found : " + path);
            response.setStatus(HttpStatus.SC_NOT_FOUND);
            ObjectMapper objectMapper = new ObjectMapper();
            String str404 = objectMapper.writeValueAsString(rsp404);
            outputStream.write(str404.getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
        } catch (Exception e){
            log.error("写入数据到response失败", e);
        }
    }

    /**
     * 子类实现可以使用 BufferedHttpEntity 类复用流
     * <code>
     *     responseEntity = new BufferedHttpEntity(responseEntity);
     * </code>
     * @param response 响应对象
     * @param responseEntity 从转发地址返回的结果实体
     */
    @SneakyThrows({IOException.class})
    protected void afterResponse(HttpServletResponse response, HttpEntity responseEntity) {
        responseEntity.writeTo(response.getOutputStream());
    }
}
