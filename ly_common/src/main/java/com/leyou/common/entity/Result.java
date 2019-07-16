package com.leyou.common.entity;

import lombok.*;


@NoArgsConstructor
@AllArgsConstructor
@Data
public class Result {
    private boolean flag;
    private Integer code;
    private String message;
    private Object data;
}
