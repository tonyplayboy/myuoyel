package com.leyou.order.config;

import com.leyou.auth.utils.RsaUtils;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.security.PublicKey;

/**
 * @Author: 98050
 * @Time: 2018-10-23 22:20
 * @Feature: jwt配置参数
 */
//@ConfigurationProperties(prefix = "leyou.jwt")
@Configuration
@RefreshScope
@Data
public class JwtProperties {

    /**
     * 公钥地址
     */
    @Value("${leyou.jwt.pubKeyPath}")
    private String pubKeyPath;

    /**
     * 公钥
     */
    private PublicKey publicKey;


    /**
     * cookie名字
     */
    @Value("${leyou.jwt.cookieName}")
    private String cookieName;



    /**
     * @PostConstruct :在构造方法执行之后执行该方法
     */
    @PostConstruct
    public void init(){
        try {
            // 获取公钥和私钥
            this.publicKey = RsaUtils.getPublicKey(pubKeyPath);
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }
}
