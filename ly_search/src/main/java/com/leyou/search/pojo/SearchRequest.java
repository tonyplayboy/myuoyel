package com.leyou.search.pojo;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;

/**
 * @Author: 98050
 * Time: 2018-10-12 20:08
 * Feature: 搜索业务对象
 */
@Data
public class SearchRequest implements Serializable {
    /**
     * 搜索条件
     */
    private String key;
    /**
     * 当前页
     */
    private Integer page;
    /**
     * 每页大小
     */
    private Integer size;
    /**
     * 排序字段
     */
    private String sortBy;

    /**
     * 是否降序
     */
    private Boolean descending;
    /**
     * 过滤字段
     */
    private Map<String,String> filter;
    /**
     * 每页大小，不从页面接收，而是固定大小
    */
    private static final Integer DEFAULT_SIZE = 20;
    /**
     * 默认页
     */
    private static final Integer DEFAULT_PAGE = 1;
    public Integer getPage() {
        if (page == null){
            return DEFAULT_PAGE;
        }
        /**
         * 获取页码时做一些校验，不能小于1
         */
        return Math.max(DEFAULT_PAGE,page);
    }
    public Integer getSize() {
        return DEFAULT_SIZE;
    }

}
