package com.leyou.order.utils;

import com.github.wxpay.sdk.WXPay;

import static com.github.wxpay.sdk.WXPayConstants.*;

import com.leyou.order.config.PayConfig;
import com.leyou.order.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author: 98050
 * @create: 2018-10-27 15:54
 **/
@Slf4j
@Component
public class PayHelper {


    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private PayConfig payConfig;

    private WXPay wxPay;

    /**
     * 生成wxPay对象
     *
     * @param payConfig
     */
    public PayHelper(PayConfig payConfig) {
        // 真实开发时
        //wxPay = new WXPay(payConfig);
        // 测试时
        this.wxPay = new WXPay(payConfig, SignType.HMACSHA256);
    }


    public String createOrder(Long orderId, Long totalFee, String desc) {
        String key = "leyou.order.pay.url." + orderId;
        try {
            String url = this.redisTemplate.opsForValue().get(key);
            if (StringUtils.isNotBlank(url)) {
                return url;
            }
        } catch (Exception e) {
            log.error("查询缓存付款链接异常,订单编号：{}", orderId, e);
        }

        try {
            Map<String, String> data = new HashMap<>();
            // 商品描述
            data.put("body", desc);
            // 订单号
            data.put("out_trade_no", orderId.toString());
            //货币
            data.put("fee_type", "CNY");
            //金额，单位是分
            data.put("total_fee", totalFee.toString());
            //调用微信支付的终端IP（estore商城的IP）
            data.put("spbill_create_ip", "127.0.0.1");
            //回调地址
            data.put("notify_url", payConfig.getNotifyUrl());
            // 交易类型为扫码支付
            data.put("trade_type", "NATIVE");
            //商品id,使用假数据
            // data.put("product_id", "1234567");

            Map<String, String> result = this.wxPay.unifiedOrder(data);

            if (SUCCESS.equals(result.get("return_code"))) {
                String url = result.get("code_url");
                // 将付款地址缓存，时间为10分钟
                try {
                    this.redisTemplate.opsForValue().set(key, url, 10, TimeUnit.MINUTES);
                } catch (Exception e) {
                    log.error("缓存付款链接异常,订单编号：{}", orderId, e);
                }
                return url;
            } else {
                log.error("创建预交易订单失败，错误信息：{}", result.get("return_msg"));
                return null;
            }
        } catch (Exception e) {
            log.error("创建预交易订单异常", e);
            return null;
        }
    }

    /**
     * 查询订单状态
     *
     * @param orderId
     * @return
     */
//    public PayState queryOrder(Long orderId) {
//        Map<String, String> data = new HashMap<>();
//        // 订单号
//        data.put("out_trade_no", orderId.toString());
//        try {
//            Map<String, String> result = this.wxPay.orderQuery(data);
//            if (result == null) {
//                // 未查询到结果，认为是未付款
//                return PayState.NOT_PAY;
//            }
//            String state = result.get("trade_state");
//            if ("SUCCESS".equals(state)) {
//                // success，则认为付款成功
//
//                // 修改订单状态
//                this.orderService.updateOrderStatus(orderId, 2);
//                return PayState.SUCCESS;
//            } else if (StringUtils.equals("USERPAYING", state) || StringUtils.equals("NOTPAY", state)) {
//                // 未付款或正在付款，都认为是未付款
//                return PayState.NOT_PAY;
//            } else {
//                // 其它状态认为是付款失败
//                return PayState.FAIL;
//            }
//        } catch (Exception e) {
//            logger.error("查询订单状态异常", e);
//            return PayState.NOT_PAY;
//        }
//    }
}
