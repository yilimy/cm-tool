package com.gomain.cm.tool.hutool;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.CircleCaptcha;
import cn.hutool.captcha.LineCaptcha;
import cn.hutool.captcha.ShearCaptcha;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

/**
 * @author caimeng
 * @date 2024/4/7 18:18
 */
@Slf4j
public class ICaptchaTest {

    /**
     * 测试：生成条形验证码
     */
    @Test
    public void genLineCaptchaTest() {
        //定义图形验证码的长和宽
        LineCaptcha lineCaptcha = CaptchaUtil.createLineCaptcha(200, 100);
        String decPath = "C:\\Users\\EDY\\Desktop\\line.png";
        //图形验证码写出，可以写出到文件，也可以写出到流
        lineCaptcha.write(decPath);
        //输出code
        log.info(lineCaptcha.getCode());
        // u4bzh
        //验证图形验证码的有效性，返回boolean值
        lineCaptcha.verify("1234");
        //重新生成验证码
        lineCaptcha.createCode();
        // 覆盖前值
        lineCaptcha.write(decPath);
        //新的验证码
        log.info(lineCaptcha.getCode());
        // 76py8
        //验证图形验证码的有效性，返回boolean值
        boolean ret = lineCaptcha.verify("1234");
        assert ret : "验证失败";
    }

    /**
     * 测试：生成圆形干扰验证码
     */
    @Test
    public void genCircleCaptchaTest() {
        //定义图形验证码的长、宽、验证码字符数、干扰元素个数
        CircleCaptcha captcha = CaptchaUtil.createCircleCaptcha(200, 100, 4, 20);
        // CircleCaptcha captcha = new CircleCaptcha(200, 100, 4, 20);
        String decPath = "C:\\Users\\EDY\\Desktop\\circle.png";
        //图形验证码写出，可以写出到文件，也可以写出到流
        captcha.write(decPath);
        log.info(captcha.getCode());
        //验证图形验证码的有效性，返回boolean值
        boolean ret = captcha.verify(captcha.getCode());
        assert ret : "验证失败1";
        ret = captcha.verify("1234");
        assert ret : "验证失败2";
    }

    /**
     * 测试：生成扭曲干扰验证码
     */
    @Test
    public void genShearCaptchaTest() {
        //定义图形验证码的长、宽、验证码字符数、干扰线宽度
        ShearCaptcha captcha = CaptchaUtil.createShearCaptcha(200, 100, 4, 4);
        //ShearCaptcha captcha = new ShearCaptcha(200, 100, 4, 4);
        //图形验证码写出，可以写出到文件，也可以写出到流
        captcha.write("C:\\Users\\EDY\\Desktop\\shear.png");
        log.info(captcha.getCode());
        //验证图形验证码的有效性，返回boolean值
        boolean ret = captcha.verify("1234");
        assert ret : "验证失败";
    }
}
