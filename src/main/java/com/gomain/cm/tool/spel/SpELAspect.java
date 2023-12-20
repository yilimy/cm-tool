package com.gomain.cm.tool.spel;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

/**
 * 针对方法的AOP操作类
 * @author caimeng
 * @date 2023/12/20 10:15
 */
@Slf4j
@Aspect
@Component
public class SpELAspect {
    /** 用于解析SpEL表达式 **/
    private final SpelExpressionParser spelExpressionParser = new SpelExpressionParser();
    /** 用户获取方法定义名 **/
    private final DefaultParameterNameDiscoverer discoverer = new DefaultParameterNameDiscoverer();

    /**
     * 打印spEL表达式的值
     * @param joinPoint 切点
     * @param ann 注解
     */
    @Before(value = "@annotation(ann)")
    private void printSpElValue(JoinPoint joinPoint, SpELAnnotation ann) {
        String fileName = valueFromSpEL(ann.fileName(), joinPoint);
        log.info("ann.fileName={}", fileName);
    }

    /**
     * 从spEl表达式中读取值
     * @param spELString 表达式
     * @param joinPoint 切点
     * @return 表达式计算的值
     */
    private String valueFromSpEL(String spELString, JoinPoint joinPoint) {
        if (!ObjectUtils.isEmpty(spELString)) {
            try {
                MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
                String[] parameterNames = discoverer.getParameterNames(methodSignature.getMethod());
                Expression expression = spelExpressionParser.parseExpression(spELString);
                EvaluationContext context = new StandardEvaluationContext();
                Object[] args = joinPoint.getArgs();
                for (int i = 0; i < args.length; i++) {
                    assert parameterNames != null;
                    context.setVariable(parameterNames[i], args[i]);
                }
                return expression.getValue(context, String.class);
            } catch (Exception e) {
                log.error("spEl读取数据失败", e);
            }
        }
        return null;
    }
}
