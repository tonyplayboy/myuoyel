package com.leyou.search.pojo;

import com.leyou.common.entity.PageResult;
import com.leyou.item.pojo.Brand;
import com.leyou.item.pojo.Category;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * @Author: 98050
 * Time: 2018-10-12 21:06
 * Feature: 搜索结果存储对象
 */
@Data
@NoArgsConstructor
public class SearchResult<T> extends PageResult<T> {

    /**
     * 分类的集合
     */
    private List<Category> categories;

    /**
     * 品牌的集合
     */
    private List<Brand> brands;

    /**
     * 规格参数的过滤条件
     */
    private List<Map<String, Object>> specs;

    public SearchResult(long total,long totalPage, List<T> item,List<Category> categories,
                        List<Brand> brands, List<Map<String,Object>> specs){
        super(total,totalPage,item);
        this.categories = categories;
        this.brands = brands;
        this.specs = specs;
    }
    public SearchResult(long total,long totalPage, List<T> item,List<Category> categories,
                        List<Brand> brands){
        super(total,totalPage,item);
        this.categories = categories;
        this.brands = brands;
    }
}
