package com.leyou.order.controller;

import com.leyou.common.entity.PageResult;
import com.leyou.order.dto.OrderDTO;
import com.leyou.order.pojo.Order;
import com.leyou.order.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * @Author: 98050
 * @Time: 2018-10-27 16:30
 * @Feature: 订单Controller
 */
@Slf4j
@RestController
@RequestMapping("order")
public class OrderController {

    @Autowired
    private OrderService orderService;



    private static int count = 0;

    /**
     * 创建订单
     *
     * @param orderDTO 订单对象
     * @return 订单编号
     */
    @PostMapping
    public String createOrder(@RequestBody @Valid OrderDTO orderDTO) {
        String orderId = this.orderService.createOrder(orderDTO).toString();
        log.info("生成的订单id: {}", orderId);
        return orderId;
    }


    /**
     * 查询订单
     *
     * @param id 订单编号
     * @return 订单对象
     */
    @GetMapping("{id}")
    public ResponseEntity<Order> queryOrderById(@PathVariable("id") Long id) {
        System.out.println("查询订单：" + id);
        Order order = this.orderService.queryOrderById(id);
        if (order == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(order);
    }

    /**
     * 根据订单id生成付款链接
     *
     * @param orderId
     * @return
     */
    @GetMapping("url/{id}")
    public String createUrl(@PathVariable("id") Long orderId) {
        return this.orderService.createUrl(orderId);
    }
//
    /**
     * 分页查询当前已经登录的用户订单
     * @param page 页数
     * @param rows 每页大小
     * @param status 订单状态
     * @return
     */
    @GetMapping("list")
    public ResponseEntity<PageResult<Order>> queryUserOrderList(
            @RequestParam(value = "page",defaultValue = "1")Integer page,
            @RequestParam(value = "rows",defaultValue = "5")Integer rows,
            @RequestParam(value = "status",required = false)Integer status
    ){

        PageResult<Order> result = this.orderService.queryUserOrderList(page,rows,status);
        if (result == null){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(result);
    }
//
//
//    /**
//     * 更新订单状态
//     * @param id
//     * @param status
//     * @return
//     */
//    @PutMapping("{id}/{status}")
//    @ApiOperation(value = "更新订单状态",notes = "更新订单状态")
//    @ApiImplicitParams({
//            @ApiImplicitParam(name = "id",value = "订单编号",type = "Long"),
//            @ApiImplicitParam(name = "status",value = "订单状态：1未付款，" +
//                    "2已付款未发货，" +
//                    "3已发货未确认，" +
//                    "4已确认未评价，" +
//                    "5交易关闭，" +
//                    "6交易成功，已评价",defaultValue = "1",type = "Integer")
//    })
//    @ApiResponses({
//            @ApiResponse(code = 204,message = "true:修改成功；false:修改状态失败"),
//            @ApiResponse(code = 400,message = "请求参数有误"),
//            @ApiResponse(code = 500,message = "服务器异常")
//    })
//    public ResponseEntity<Boolean> updateOrderStatus(@PathVariable("id") Long id, @PathVariable("status") Integer status){
//        Boolean result = this.orderService.updateOrderStatus(id,status);
//        if (!result){
//            //返回400
//            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
//        }
//        //返回204
//        return new ResponseEntity<>(result, HttpStatus.NO_CONTENT);
//    }
//

    /**
     * 查询付款状态
     * @param orderId
     * @return
     */
    @GetMapping("status/{id}")
    public Integer queryOrderStatusById(@PathVariable("id") Long orderId){
        return orderService.queryOrderStatusById(orderId);
    }
//
//    /**
//     * 根据订单id查询其包含的skuId
//     * @param id
//     * @return
//     */
//    @GetMapping("skuId/{id}")
//    @ApiOperation(value = "根据订单号查询其包含的所有商品ID",notes = "查询商品ID")
//    @ApiImplicitParam(name = "id",value = "订单编号",type = "Long")
//    @ApiResponses({
//            @ApiResponse(code = 200,message = "商品订单号集合"),
//            @ApiResponse(code = 404,message = "没有找到对应的订单号集合"),
//            @ApiResponse(code = 500,message = "服务器异常")
//    })
//    public ResponseEntity<List<Long>> querySkuIdByOrderId(@PathVariable("id") Long id){
//        List<Long> longList = this.orderService.querySkuIdByOrderId(id);
//        if (longList == null || longList.size() == 0){
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
//        }
//        return ResponseEntity.ok(longList);
//    }
//
//
//    /**
//     * 根据订单id查询订单状态
//     * @param id
//     * @return
//     */
//    @GetMapping("status/{id}")
//    @ApiOperation(value = "根据订单号查询订单状态",notes = "查询订单状态")
//    @ApiImplicitParam(name = "id",value = "订单编号",type = "Long")
//    @ApiResponses({
//            @ApiResponse(code = 200,message = "订单状态"),
//            @ApiResponse(code = 404,message = "没有找到对应的订单状态"),
//            @ApiResponse(code = 500,message = "服务器异常")
//    })
//    public ResponseEntity<OrderStatus> queryOrderStatusById(@PathVariable("id") Long id){
//        OrderStatus orderStatus = this.orderService.queryOrderStatusById(id);
//        if (orderStatus == null){
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
//        }
//        return ResponseEntity.ok(orderStatus);
//    }
}
