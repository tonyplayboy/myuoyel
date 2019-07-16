package com.leyou.sms.listener;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SmsListenerTest {

    @Autowired
    private AmqpTemplate amqpTemplate;

    @Test
    public void listenSms() {
        Map<String, String> msg = new HashMap<>();
        msg.put("phone", "17765287397");
        msg.put("code", "123456");
        amqpTemplate.convertAndSend("leyou.sms.exchange", "sms.verify.code", msg);
    }
}