package com.leyou.order.controller;

import com.leyou.order.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("notify")
public class NotifyController {

    @Autowired
    private OrderService orderService;

    @PostMapping("pay")
    public Map<String, String> handleNotify(@RequestBody Map<String, String> requestMap) {

        boolean b;
        try {
            b = orderService.handleNotify(requestMap);
        } catch (Exception e) {
            log.error(e.getMessage());
            b = false;
        }
        Map<String, String> responseMap = new HashMap<>();
        if (b) {
            responseMap.put("return_code", "SUCCESS");
            responseMap.put("return_msg", "OK");
        }else {
            responseMap.put("return_code", "Fail");
            responseMap.put("return_msg", "Fail");
        }

        return responseMap;
    }
}
