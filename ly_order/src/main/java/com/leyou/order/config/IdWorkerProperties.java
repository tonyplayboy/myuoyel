package com.leyou.order.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

/**
 * @author: 98050
 * @create: 2018-10-27
 **/
@ConfigurationProperties(prefix = "leyou.worker")
@RefreshScope
@Data
public class IdWorkerProperties {

    /**
     * 当前机器id
     */
    //@Value("${leyou.worker.workerId}")
    private long workerId;

    /**
     * 序列号
     */
    //@Value("${leyou.worker.dataCenterId}")
    private long dataCenterId;


}
