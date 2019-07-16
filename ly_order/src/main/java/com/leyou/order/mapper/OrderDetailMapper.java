package com.leyou.order.mapper;

import com.leyou.order.pojo.OrderDetail;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.common.Mapper;
import tk.mybatis.mapper.common.special.InsertListMapper;

/**
 * @Author: 98050
 * @Time: 2018-10-27 16:33
 * @Feature:
 */
@Component
public interface OrderDetailMapper extends Mapper<OrderDetail>, InsertListMapper<OrderDetail> {

}
