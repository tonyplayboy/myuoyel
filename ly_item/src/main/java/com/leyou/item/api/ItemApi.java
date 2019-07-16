package com.leyou.item.api;

import com.leyou.common.entity.PageResult;
import com.leyou.item.pojo.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @Author: 98050
 * Time: 2018-10-11 20:04
 * Feature:品牌服务接口
 */
public interface ItemApi {
    /**
     * 根据品牌id集合，查询品牌信息
     * @param id
     * @return
     */
    @GetMapping("brand/{id}")
    Brand queryBrandById(@PathVariable("id") Long id);

    /**
     * 根据id，查询分类名称
     * @param ids
     * @return
     */
    @GetMapping("category/names")
    List<String> queryNameByIds(@RequestParam("ids") List<Long> ids);

    /**
     * 根据分类id集合查询分类名称
     * @param ids
     * @return
     */
    @GetMapping("category/all")
    List<Category> queryCategoryByIds(@RequestParam("ids") List<Long> ids);

    /**
     * 分页查询
     * @param page
     * @param rows
     * @param sortBy
     * @param desc
     * @param key
     * @param saleable
     * @return
     */
    @GetMapping("goods/spu/page")
    PageResult<Spu> querySpuByPage(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "rows", defaultValue = "5") Integer rows,
            @RequestParam(value = "sortBy", required = false) String sortBy,
            @RequestParam(value = "desc", defaultValue = "false") Boolean desc,
            @RequestParam(value = "key", required = false) String key,
            @RequestParam(value = "saleable", defaultValue = "true") Boolean saleable);
    /**
     * 根据spu商品id查询详情
     * @param id
     * @return
     */
    @GetMapping("goods/spu/detail/{id}")
    SpuDetail querySpuDetailBySpuId(@PathVariable("id") Long id);

    /**
     * 根据Spu的id查询其下所有的sku
     * @param id
     * @return
     */
    @GetMapping("goods/sku/list/{id}")
    List<Sku> querySkuBySpuId(@PathVariable("id") Long id);
    /**
     * 根据Spu的id查询其下所有的sku
     * @param ids
     * @return
     */
    @GetMapping("goods/sku/list/ids")
    List<Sku> querySkuByIds(@RequestParam("id") List<Long> ids);

    /**
     * 根据id查询商品
     * @param id
     * @return
     */
    @GetMapping("goods/spu/{id}")
    SpuBo queryGoodById(@PathVariable("id") Long id);

    /**
     * 根据sku的id查询sku
     * @param id
     * @return
     */
    @GetMapping("goods/sku/{id}")
    Sku querySkuById(@PathVariable("id") Long id);
    /**
     * 查询商品分类对应的规格参数模板
     * @param id
     * @return
     */
    @GetMapping("spec/{id}")
    String querySpecificationByCategoryId(@PathVariable("id") Long id);


    /**
     * 分页查询
     * @param page
     * @param rows
     * @param desc
     * @param saleable
     * @return
     */
    @GetMapping("spu/page")
    PageResult<Spu> querySpuByPage(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "rows", defaultValue = "5") Integer rows,
            @RequestParam(value = "desc", defaultValue = "false") Boolean desc,
            @RequestParam(value = "saleable", defaultValue = "true") Boolean saleable);

    /**
     * 根据品牌id集合，查询品牌信息
     * @param ids
     * @return
     */
    @GetMapping("brand/list")
    List<Brand> queryBrandByIds(@RequestParam("ids") List<Long> ids);



}
