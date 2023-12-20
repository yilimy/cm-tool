package com.gomain.cm.tool.spel;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记在方法上的注解，用于支持SpEL表达式
 * @author caimeng
 * @date 2023/12/20 9:40
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SpELAnnotation {

    /**
     * @return 文件名
     */
    String fileName() default "";

}
