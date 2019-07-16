package com.leyou.order.enums;

import lombok.AllArgsConstructor;

/**
 * @author: HuYi.Zhang
 * @create: 2018-06-07 17:44
 **/
@AllArgsConstructor
public enum PayState {

    NOT_PAY(0, "未付款"),
    SUCCESS(1, "支付成功"),
    FAIL(2, "支付失败")
    ;

    int value;
    String msg;

    public int getValue() {
        return value;
    }

    public String getMsg() {
        return msg;
    }
}
