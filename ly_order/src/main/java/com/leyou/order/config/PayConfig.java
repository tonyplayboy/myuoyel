package com.leyou.order.config;

import com.github.wxpay.sdk.WXPay;
import com.github.wxpay.sdk.WXPayConfig;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.io.InputStream;

/**
 * @author: 98050
 * @create: 2018-10-27
 **/
@Data
@Component
@Configuration
public class PayConfig implements WXPayConfig {


    @Value("${leyou.pay.appId}")
    private String appId; //公众账号ID
    @Value("${leyou.pay.mchId}")
    private String mchId; //商户号
    @Value("${leyou.pay.key}")
    private String key; //生成签名的密钥
    @Value("${leyou.pay.connectTimeoutMs}")
    private int connectTimeoutMs; //连接超时时间
    @Value("${leyou.pay.readTimeoutMs}")
    private int readTimeoutMs; //读取超时时间
    @Value("${leyou.pay.notifyUrl}")
    private String notifyUrl;




    @Override
    public String getAppID() {
        return this.appId;
    }

    @Override
    public String getMchID() {
        return this.mchId;
    }

    @Override
    public String getKey() {
        return this.key;
    }

    @Override
    public InputStream getCertStream() {
        return null;
    }

    @Override
    public int getHttpConnectTimeoutMs() {
        return this.connectTimeoutMs;
    }

    @Override
    public int getHttpReadTimeoutMs() {
        return this.readTimeoutMs;
    }

}
