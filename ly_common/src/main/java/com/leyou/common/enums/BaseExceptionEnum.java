package com.leyou.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum BaseExceptionEnum {
    SERVER_ERROR(500, "服务器内部错误"),
    PRICE_CANNOT_BE_NULL(400, "价格不能为空"),
    PRICE_CANNOT_BE_ZERO(401, "价格不能为零"),
    ;
    private int code;
    private String msg;



}
