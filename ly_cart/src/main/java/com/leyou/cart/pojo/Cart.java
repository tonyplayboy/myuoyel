package com.leyou.cart.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: 98050
 * @Time: 2018-10-25 20:27
 * @Feature: 购物车实体类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Cart {
    /**
     * 用户Id
     */
    private Long userId;

    /**
     * 商品id
     */
    private Long skuId;

    /**
     * 标题
     */
    private String title;

    /**
     * 图片
     */
    private String image;

    /**
     * 加入购物车时的价格
     */
    private Long price;

    /**
     * 购买数量
     */
    private Integer num;

    /**
     * 商品规格参数
     */
    private String ownSpec;


}
