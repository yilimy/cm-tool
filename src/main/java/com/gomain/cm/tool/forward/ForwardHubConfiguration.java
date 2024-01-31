package com.gomain.cm.tool.forward;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ObjectUtils;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.servlet.Filter;
import java.util.List;

/**
 * 转发省枢纽的配置类
 * @author caimeng
 * @date 2024/1/31 9:51
 */
@Configuration
@ConditionalOnProperty(value = "forward.sub.enabled")
public class ForwardHubConfiguration {
    /**
     * 转发uri列表
     */
    @Value("#{'${forward.sub.uris:}'.split(',')}")
    private String[] uris;

    /**
     * 转发服务异常处理器 bean 注入
     * @return 转发服务异常处理器
     */
    @Bean
    public ForwardHubResolver forwardResolver() {
        return new ForwardHubResolver();
    }

    /**
     * 转发异常处理器注册到 spring mvc 配置中心
     * @param resolver 转发服务异常处理器
     * @return spring mvc 配置类
     */
    @Bean
    public WebMvcConfigurer forwardResolverMvcConfig(ForwardHubResolver resolver) {
        return new WebMvcConfigurer() {
            @Override
            public void extendHandlerExceptionResolvers(List<HandlerExceptionResolver> exceptionResolvers) {
                // 添加针对转发业务的异常处理器
                exceptionResolvers.add(resolver);
            }
        };
    }

    /**
     * 注入转发拦截器
     * @param exceptionResolver 异常处理器
     * @param frontForwardResolver 自定义异常处理器
     * @param connectionRequestTimeout 请求超时时间
     * @param connectTimeout 连接超时时间
     * @param socketTimeout socket超时时间
     * @param hubHost 省枢纽地址
     * @param targetAppKey 省枢纽appKey
     * @param targetAppSecret 省枢纽appSecret
     * @param headerExclude 请求头排除项
     * @return 拦截器
     */
    @Bean
    @ConditionalOnMissingBean
    public ForwardHubFilter forwardHubFilter(
            // Spring 提供的默认异常解析器
            @Autowired @Qualifier("handlerExceptionResolver")
            HandlerExceptionResolver exceptionResolver,
            // 自定义异常解析器
            ForwardHubResolver frontForwardResolver,
            @Value("${forward.sub.timeout.request:120000}")
            int connectionRequestTimeout,
            @Value("${forward.sub.timeout.connect:120000}")
            int connectTimeout,
            @Value("${forward.sub.timeout.socket:120000}")
            int socketTimeout,
            @Value("${forward.sub.host:}")
            String hubHost,
            @Value("${forward.sub.app.key:}")
            String targetAppKey,
            @Value("${forward.sub.app.secret:}")
            String targetAppSecret,
            @Value("#{'${forward.sub.header.exclude:}'.trim().split(',')}")
            List<String> headerExclude) {
        ForwardHubFilter forwardHubFilter = new ForwardHubFilter(exceptionResolver, frontForwardResolver);
        if (connectionRequestTimeout != 0) {
            forwardHubFilter.setConnectionRequestTimeout(connectionRequestTimeout);
        }
        if (connectTimeout != 0) {
            forwardHubFilter.setConnectTimeout(connectTimeout);
        }
        if (socketTimeout != 0) {
            forwardHubFilter.setSocketTimeout(socketTimeout);
        }
        if (!ObjectUtils.isEmpty(headerExclude)) {
            boolean empty = headerExclude.size() == 1 && "".equals(headerExclude.get(0));
            if (!empty) {
                forwardHubFilter.setHeaderExclude(headerExclude);
            }
        }
        if (!ObjectUtils.isEmpty(hubHost)) {
            ForwardHubFilter.setHubHost(hubHost);
        }
        if (!ObjectUtils.isEmpty(targetAppKey)) {
            ForwardHubFilter.setTargetAppKey(targetAppKey);
        }
        if (!ObjectUtils.isEmpty(targetAppSecret)) {
            ForwardHubFilter.setTargetAppSecret(targetAppSecret);
        }
        return forwardHubFilter;
    }

    /**
     * 注册拦截器
     * @param forwardHubFilter 拦截器
     * @return 过滤器
     */
    @Bean
    @ConditionalOnBean(ForwardHubFilter.class)
    public FilterRegistrationBean<?> frontFilterRegistration(ForwardHubFilter forwardHubFilter) {
        FilterRegistrationBean<? super Filter> registration = new FilterRegistrationBean<>();
        // 注入过滤器
        registration.setFilter(forwardHubFilter);
        // 拦截地址不能为空
        String emptyMsg  = "配置forward.sub.uris为空";
        if (ObjectUtils.isEmpty(uris)) {
            throw new RuntimeException(emptyMsg);
        }
        boolean empty = uris.length == 1 && "".equals(uris[0]);
        if (empty) {
            throw new RuntimeException(emptyMsg);
        }
        // 设置拦截规则
        registration.addUrlPatterns(uris);
        // 设置拦截名称
        registration.setName("forwardHubFilter");
        // 设置拦截顺序
        registration.setOrder(1);
        return registration;
    }

}
