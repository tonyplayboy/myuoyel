package com.leyou.search.client;

import com.leyou.item.api.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;

/**
 * @Author: 98050
 * Time: 2018-10-11 20:49
 * Feature:品牌FeignClient
 */
@Component
@FeignClient(value = "ly-item")
public interface ItemClient extends ItemApi{
}
