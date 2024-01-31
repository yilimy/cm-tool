package com.gomain.cm.tool.forward;

import com.alibaba.fastjson.JSON;
import com.gomain.cm.tool.pojo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Optional;

/**
 * 转发到省枢纽的Servlet异常处理器
 * @author caimeng
 * @date 2024/1/31 9:54
 */
@Slf4j
public class ForwardHubResolver implements HandlerExceptionResolver {
    @Override
    public ModelAndView resolveException(HttpServletRequest request,
                                         HttpServletResponse response,
                                         Object handler,
                                         Exception ex) {
        if (Objects.nonNull(handler) && handler instanceof ForwardHubResolver) {
            log.info("forward resolveException ... ");
            String errorMsg = Optional.of(ex).map(Exception::getMessage).orElse("转发失败");
            Result<?> result = Result.fail().setMessage(errorMsg);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            try (PrintWriter out = response.getWriter()) {
                response.setContentType(MediaType.APPLICATION_JSON.getType());
                out.write(JSON.toJSONString(result));
            } catch (Exception e) {
                log.error("IO错误", e);
            }
            return new ModelAndView();
        }
        return null;
    }
}
