package com.leyou.common.exception;

import com.leyou.common.enums.BaseExceptionEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LyException extends RuntimeException {
    private BaseExceptionEnum baseExceptionEnum;
}
