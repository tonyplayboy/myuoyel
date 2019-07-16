package com.leyou.sms.pojo;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

/**
 * @Author: 98050
 * @Time: 2018-10-22 18:34
 * @Feature: 短信服务实体类
 */
//@ConfigurationProperties(prefix = "leyou.sms")
@Data
@Configuration
@RefreshScope
public class SmsProperties {

    @Value("${leyou.sms.accessKeyId}")
    private String accessKeyId;

    @Value("${leyou.sms.accessKeySecret}")
    private String accessKeySecret;

    @Value("${leyou.sms.signName}")
    private String signName;

    @Value("${leyou.sms.verifyCodeTemplate}")
    private String verifyCodeTemplate;




}
