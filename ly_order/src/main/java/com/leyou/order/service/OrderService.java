package com.leyou.order.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.wxpay.sdk.WXPayConstants;
import com.github.wxpay.sdk.WXPayUtil;
import com.leyou.auth.entity.UserInfo;
import com.leyou.common.entity.PageResult;
import com.leyou.common.utils.IdWorker;
import com.leyou.item.pojo.Sku;
import com.leyou.item.pojo.Stock;
import com.leyou.order.client.ItemClient;
import com.leyou.order.config.PayConfig;
import com.leyou.order.dto.CartDTO;
import com.leyou.order.dto.OrderDTO;
import com.leyou.order.enums.OrderStatusEnum;
import com.leyou.order.interceptor.LoginInterceptor;
import com.leyou.order.mapper.OrderDetailMapper;
import com.leyou.order.mapper.OrderMapper;
import com.leyou.order.mapper.OrderStatusMapper;
import com.leyou.order.pojo.Address;
import com.leyou.order.pojo.Order;
import com.leyou.order.pojo.OrderDetail;
import com.leyou.order.pojo.OrderStatus;
import com.leyou.common.utils.IdWorker;
import com.leyou.order.utils.PayHelper;
import com.sun.xml.internal.bind.v2.TODO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.stream.Collectors;

import static com.github.wxpay.sdk.WXPayConstants.SUCCESS;

/**
 * @Author: 98050
 * @Time: 2018-10-27 16:37
 * @Feature:
 */
@Slf4j
@Service
public class OrderService {
    @Autowired
    private IdWorker idWorker;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderStatusMapper orderStatusMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private AddressService addressService;
    @Autowired
    private ItemClient itemClient;
    @Autowired
    private PayHelper payHelper;
    @Autowired
    private PayConfig payConfig;

    @Transactional()
    public Long createOrder(OrderDTO orderDTO) {
        //创建订单
        Order order = new Order();
        //1.生成orderId
        long orderId = idWorker.nextId();
        order.setOrderId(orderId);
        //2.获取登录的用户
        UserInfo userInfo = LoginInterceptor.getLoginUser();
        //3.初始化数据
        order.setUserId(userInfo.getId());
        order.setBuyerNick(userInfo.getUsername());
        order.setBuyerRate(false);
        order.setCreateTime(new Date());
        order.setPaymentType(orderDTO.getPaymentType());
        //4. 获取收货人信息
        Address address = addressService.queryAddressById(orderDTO.getAddressId());
        order.setReceiver(address.getName());
        order.setReceiverAddress(address.getAddress());
        order.setReceiverCity(address.getCity());
        order.setReceiverDistrict(address.getDistrict());
        order.setReceiverMobile(address.getPhone());
        order.setReceiverState(address.getState());
        order.setReceiverZip(address.getZipCode());
        //5. 金额
        //把cartDto转为一个map, key是sku的id,值是num
        Map<Long, Integer> numMap = orderDTO.getCarts()
                .stream().collect(Collectors.toMap(CartDTO::getSkuId, CartDTO::getNum));
        Set<Long> ids = numMap.keySet();
        List<Sku> skus = itemClient.querySkuByIds(new ArrayList<>(ids));
        //准备orderDetail集合
        List<OrderDetail> details = new ArrayList<>();
        long totalPay = 0L;
        for (Sku sku : skus) {
            totalPay += sku.getPrice() * numMap.get(sku.getId());
            //封装orderDetail
            OrderDetail detail = new OrderDetail();
            detail.setImage(StringUtils.substringBefore(sku.getImages(), ","));
            detail.setNum(numMap.get(sku.getId()));
            detail.setOrderId(orderId);
            detail.setOwnSpec(sku.getOwnSpec());
            detail.setPrice(sku.getPrice());
            detail.setSkuId(sku.getId());
            detail.setTitle(sku.getTitle());
            details.add(detail);
        }
        order.setTotalPay(totalPay);
        order.setActualPay(totalPay + order.getPostFee() - 0L);

        //4.保存数据
        this.orderMapper.insertSelective(order);
        //8.保存订单详情，使用批量插入功能
        this.orderDetailMapper.insertList(details);
        //5.保存订单状态
        OrderStatus orderStatus = new OrderStatus();
        orderStatus.setOrderId(orderId);
        orderStatus.setCreateTime(order.getCreateTime());
        //初始状态未未付款：1
        orderStatus.setStatus(OrderStatusEnum.INIT.getCode());
        //6.保存数据
        this.orderStatusMapper.insertSelective(orderStatus);
        //7. 减库存
        // TODO
        //7.在订单详情中添加orderId
//        order.getOrderDetails().forEach(orderDetail -> {
//            //添加订单
//            orderDetail.setOrderId(orderId);
//        });


        //order.getOrderDetails().forEach(orderDetail -> this.stockMapper.reduceStock(orderDetail.getSkuId(), orderDetail.getNum()));

        return orderId;

    }

    /**
     * 根据订单号查询订单
     *
     * @param id
     * @return
     */
    public Order queryOrderById(Long id) {
        //1.查询订单
        Order order = this.orderMapper.selectByPrimaryKey(id);
        //2.查询订单详情
        Example example = new Example(OrderDetail.class);
        example.createCriteria().andEqualTo("orderId", id);
        List<OrderDetail> orderDetail = this.orderDetailMapper.selectByExample(example);
        orderDetail.forEach(System.out::println);
        //3.查询订单状态
        OrderStatus orderStatus = this.orderStatusMapper.selectByPrimaryKey(order.getOrderId());
        //4.order对象填充订单详情
        order.setOrderDetails(orderDetail);
        //5.order对象设置订单状态
        order.setOrderStatus(orderStatus);
        //6.返回order
        return order;
    }

    /**
     * 根据订单号生成付款url
     *
     * @param id
     * @return
     */
    public String createUrl(Long id) {
        //1.查询订单
        Order order = this.queryOrderById(id);
        Integer status = order.getOrderStatus().getStatus();
        if (OrderStatusEnum.INIT.getCode() == status) {
            //return this.payHelper.createOrder(id, order.getActualPay(), "黑马测试生成付款链接");
            return this.payHelper.createOrder(id, 1L, "黑马测试生成付款链接");
        }
        return null;


    }

    public boolean handleNotify(Map<String, String> requestMap) throws Exception {
        //数据校验
        if (!SUCCESS.equals(requestMap.get("return_code"))) {
            return false;
        }
        //校验签名
        String sign1 = WXPayUtil.generateSignature(requestMap, payConfig.getKey(), WXPayConstants.SignType.HMACSHA256);
        String sign2 = WXPayUtil.generateSignature(requestMap, payConfig.getKey(), WXPayConstants.SignType.MD5);
        String sign = requestMap.get("sign");

        if (!(sign.equals(sign1) || sign.equals(sign2))) {
            return false;
        }
        //比较金额
        String totalFee = requestMap.get("total_fee");
        String orderId = requestMap.get("out_trade_no");
        if (StringUtils.isBlank(totalFee)) {
            return false;
        }
        if (StringUtils.isBlank(orderId)) {
            return false;
        }
        Order order = this.queryOrderById(Long.valueOf(orderId));
        if (order == null) {
            return false;
        }
//                    if(!StringUtils.equals(totalFee, order.getActualPay().toString())) {
//                        return false;
//                    }
        if (!StringUtils.equals(totalFee, "1")) {
            return false;
        }
        //更新订单状态
        updateOrderStatus(order.getOrderId(), OrderStatusEnum.PAYED.getCode());

        return true;
    }

//
    /**
     * 查询当前登录用户的订单，通过订单状态进行筛选
     * @param page
     * @param rows
     * @param status
     * @return
     */
    public PageResult<Order> queryUserOrderList(Integer page, Integer rows, Integer status) {
        try{
            //1.分页
            PageHelper.startPage(page,rows);
            //2.获取登录用户
            UserInfo userInfo = LoginInterceptor.getLoginUser();
            //3.查询
            Page<Order> pageInfo = (Page<Order>) this.orderMapper.queryOrderList(userInfo.getId(), status);
            //4.填充orderDetail
            List<Order> orderList = pageInfo.getResult();
            orderList.forEach(order -> {
                Example example = new Example(OrderDetail.class);
                example.createCriteria().andEqualTo("orderId",order.getOrderId());
                List<OrderDetail> orderDetailList = this.orderDetailMapper.selectByExample(example);
                order.setOrderDetails(orderDetailList);
            });
            return new PageResult<>(pageInfo.getTotal(),(long)pageInfo.getPages(), orderList);
        }catch (Exception e){
            log.error("查询订单出错",e);
            return null;
        }
    }

    /**
     * 更新订单状态
     * @param orderId
     * @param status
     */
    public void updateOrderStatus(Long orderId, Integer status) {
        //UserInfo userInfo = LoginInterceptor.getLoginUser();
        //Long spuId = this.goodsClient.querySkuById(findSkuIdByOrderId(id)).getSpuId();

        OrderStatus orderStatus = new OrderStatus();
        orderStatus.setOrderId(orderId);
        orderStatus.setStatus(status);

        //延时消息
        //OrderStatusMessage orderStatusMessage = new OrderStatusMessage(id,userInfo.getId(),userInfo.getUsername(),spuId,1);
        //OrderStatusMessage orderStatusMessage2 = new OrderStatusMessage(id,userInfo.getId(),userInfo.getUsername(),spuId,2);
        //1.根据状态判断要修改的时间
        switch (status){
            case 2:
                //2.付款时间
                orderStatus.setPaymentTime(new Date());
                break;
            case 3:
                //3.发货时间
                orderStatus.setConsignTime(new Date());
                //发送消息到延迟队列，防止用户忘记确认收货
                //orderStatusService.sendMessage(orderStatusMessage);
                //orderStatusService.sendMessage(orderStatusMessage2);
                break;
            case 4:
                //4.确认收货，订单结束
                orderStatus.setEndTime(new Date());
                //orderStatusService.sendMessage(orderStatusMessage2);
                break;
            case 5:
                //5.交易失败，订单关闭
                orderStatus.setCloseTime(new Date());
                break;
            case 6:
                //6.评价时间
                orderStatus.setCommentTime(new Date());
                break;

                default:
        }
        this.orderStatusMapper.updateByPrimaryKeySelective(orderStatus);
    }
//
//    /**
//     * 根据订单号查询商品id
//     * @param id
//     * @return
//     */
//    public List<Long> querySkuIdByOrderId(Long id) {
//        Example example = new Example(OrderDetail.class);
//        example.createCriteria().andEqualTo("orderId",id);
//        List<OrderDetail> orderDetailList = this.orderDetailMapper.selectByExample(example);
//        List<Long> ids = new ArrayList<>();
//        orderDetailList.forEach(orderDetail -> ids.add(orderDetail.getSkuId()));
//        return ids;
//    }
//
    /**
     * 根据订单号查询订单状态
     * @param id
     * @return
     */
    public Integer queryOrderStatusById(Long id) {
        return this.orderStatusMapper.selectByPrimaryKey(id).getStatus();
    }
//
//    /**
//     * 查询订单下商品的库存，返回库存不足的商品Id
//     * @param order
//     * @return
//     */
//    public List<Long> queryStock(Order order) {
//        List<Long> skuId = new ArrayList<>();
//        order.getOrderDetails().forEach(orderDetail -> {
//            Stock stock = this.stockMapper.selectByPrimaryKey(orderDetail.getSkuId());
//            if (stock.getStock() - orderDetail.getNum() < 0){
//                //先判断库存是否充足
//                skuId.add(orderDetail.getSkuId());
//            }
//        });
//        return skuId;
//    }
//
//    /**
//     * 根据订单id查询其skuId
//     * @param id
//     * @return
//     */
//    public Long findSkuIdByOrderId(Long id){
//        Example example = new Example(OrderDetail.class);
//        example.createCriteria().andEqualTo("orderId", id);
//        List<OrderDetail> orderDetail = this.orderDetailMapper.selectByExample(example);
//        return orderDetail.get(0).getSkuId();
//    }


}
