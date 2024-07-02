package com.gomain.cm.tool.date;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

/**
 * @author caimeng
 * @date 2024/4/11 10:11
 */
@Slf4j
public class DateTimeTest {

    @Test
    public void dateTimeTest() {
        String gmtTime = "20240411015546Z";
        dateToLocal(gmtTime);
    }

    /**
     * 签名时间转码
     * @param time UTC或GMT时间
     * @return yyyy-MM-dd HH:mm:ss
     */
    private String dateToLocal(String time) {
        String dateStr = time;
        if (!Objects.isNull(time)) {
            switch (time.length()) {
                case 15:
                    dateStr = gmtToLocal(time);
                    break;
                case 13:
                    dateStr = utcToLocal(time);
            }
        }
        return dateStr;
    }

    /**
     * 将GMT时间转成日期
     *
     * @param gmtTimeStr yyyyMMddHHmmss'Z'
     * @return yyyy-MM-dd HH:mm:ss
     */
    private String gmtToLocal(String gmtTimeStr) {
        try {
            log.info("GMT时间转日期:{}", gmtTimeStr);
            SimpleDateFormat gmtSDF = new SimpleDateFormat("yyyyMMddHHmmss'Z'");
            // org.bouncycastle.asn1.ASN1UTCTime#getAdjustedDate()
            gmtSDF.setTimeZone(new SimpleTimeZone(0,"Z"));
            Date date = gmtSDF.parse(gmtTimeStr);
            SimpleDateFormat localSDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String dateStr = localSDF.format(date);
            log.info("dateStr={}", dateStr);
            return dateStr;
        } catch (Exception e) {
            log.error("GMT时间转换失败", e);
        }
        return null;
    }

    /**
     * 将UTC时间转成日期
     *
     * @param utcTimeStr yyMMddHHmmss'Z'
     * @return yyyy-MM-dd HH:mm:ss
     */
    private String utcToLocal(String utcTimeStr) {
        try {
            log.info("UTC时间转日期:{}", utcTimeStr);
            SimpleDateFormat utcSDF = new SimpleDateFormat("yyMMddHHmmss'Z'");
            // 按 org.bouncycastle.asn1.ASN1UTCTime#getDate() 中的代码，是不需要 setTimeZone 的
            utcSDF.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date date = utcSDF.parse(utcTimeStr);
            SimpleDateFormat localSDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String dateStr = localSDF.format(date);
            log.info("dateStr={}", dateStr);
            return dateStr;
        } catch (Exception e) {
            log.error("UTC时间转换失败", e);
        }
        return null;
    }
}
