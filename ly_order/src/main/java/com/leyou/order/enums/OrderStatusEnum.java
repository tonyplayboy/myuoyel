package com.leyou.order.enums;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum OrderStatusEnum {
    INIT(1, "初始化,未付款"),
    PAYED(2, "已付款,未发货"),
    DELIVERED(3, "已发货,未确认"),
    CONFIRMED(4, "已确认,未评价"),
    CLOSED(5, "已关闭"),
    RATED(6, "已评价,交易结束"),
    ;
    private Integer code;
    private String msg;

    public Integer getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
