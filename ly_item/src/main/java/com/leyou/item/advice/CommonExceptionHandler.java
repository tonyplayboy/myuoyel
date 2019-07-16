package com.leyou.item.advice;

import com.leyou.common.entity.Result;
import com.leyou.common.exception.LyException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@ControllerAdvice
@RestController
public class CommonExceptionHandler {


    @ExceptionHandler(LyException.class)
    @ResponseBody
    public Result handleException(LyException e) {
        return new Result(
                false,
                e.getBaseExceptionEnum().getCode(),
                e.getBaseExceptionEnum().getMsg(),
                e.getBaseExceptionEnum().getMsg());
    }
}
