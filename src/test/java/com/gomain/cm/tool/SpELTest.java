package com.gomain.cm.tool;

import cn.hutool.core.map.MapUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.ParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * SpEL表达式单元测试
 * @author caimeng
 * @date 2024/3/11 11:11
 */
@Slf4j
public class SpELTest {

    /**
     * 循环SpEL表达式中的值
     */
    @Test
    public void parseListTest() {
//        String content = "#{ #list?.![#this + '1'] }";
        String content = "#{ #list?.![#this + 1] }";
        ExpressionParser parser = new SpelExpressionParser();
        Expression expression = parser.parseExpression(content, ParserContext.TEMPLATE_EXPRESSION);
        EvaluationContext context = new StandardEvaluationContext();
//        List<String> resList = new ArrayList<>(Arrays.asList("1", "2", "3", "5", "7", "9"));
        List<Integer> resList = new ArrayList<>(Arrays.asList(1, 2, 3, 5, 7, 9));
        context.setVariable("list", resList);
        List<?> value = expression.getValue(context, List.class);
//        Object value = expression.getValue();
        log.info("value = {}", value);
    }

    /**
     * SpEL解析map
     */
    @Test
    public void parseMapTest() {
        Map<String, String> map = MapUtil.<String, String>builder()
                .put("key1", "value1")
                .put("key2", "value2")
                .put("key3", "value3")
                .put("key4", "value4")
                .build();
        EvaluationContext context = new StandardEvaluationContext();
        context.setVariable("map", map);

        String content = "#{ #map['key1'] }";
        ExpressionParser parser = new SpelExpressionParser();
        Expression expression = parser.parseExpression(content, ParserContext.TEMPLATE_EXPRESSION);
        String value = expression.getValue(context, String.class);
        // value1
        System.out.println(value);

        // 取不到值时，返回null
        content = "#{ #map['asd'] }";
        expression = parser.parseExpression(content, ParserContext.TEMPLATE_EXPRESSION);
        String value2 = expression.getValue(context, String.class);
        assert value2 == null;

        // 修改map
        content = "#{ #map['key4'] = 'edited' }";
        expression = parser.parseExpression(content, ParserContext.TEMPLATE_EXPRESSION);
        String value3 = expression.getValue(context, String.class); // map调用的是put方法修改value值
        // edited
        System.out.println(value3);
        // {key1=value1, key2=value2, key3=value3, key4=edited}
        log.info("map edited : {}", map);

        // map迭代，并转成list
        content = "#{ #map.![#this.key + '-' + #this.value] }";
        expression = parser.parseExpression(content, ParserContext.TEMPLATE_EXPRESSION);
        List<?> value4 = expression.getValue(context, List.class);
        // [key1-value1, key2-value2, key3-value3, key4-edited]
        log.info("value4 = {}", value4);

        // 对map的筛选, key or value
        content = "#{ #map.?[#this.value.contains('value')] }";
        expression = parser.parseExpression(content, ParserContext.TEMPLATE_EXPRESSION);
        Map<?,?> value5 = expression.getValue(context, Map.class);
        // {key1=value1, key2=value2, key3=value3}
        log.info("value5 = {}", value5);
    }

    /**
     * 测试结果是否为空
     */
    @Test
    public void valueNullTest() {
        String content = "#{ '${spring.redis.cluster.nodes}' }";
        ExpressionParser parser = new SpelExpressionParser();
        Expression expression = parser.parseExpression(content, ParserContext.TEMPLATE_EXPRESSION);
        String value = expression.getValue(String.class);
        System.out.println(value);
    }

}
