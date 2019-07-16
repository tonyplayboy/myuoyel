package com.leyou.item.controller;


import com.leyou.common.entity.PageResult;
import com.leyou.common.pojo.SpuQueryByPageParameter;
import com.leyou.item.pojo.Sku;
import com.leyou.item.pojo.Spu;
import com.leyou.item.pojo.SpuBo;
import com.leyou.item.pojo.SpuDetail;
import com.leyou.common.pojo.SpuQueryByPageParameter;
import com.leyou.item.service.CategoryService;
import com.leyou.item.service.GoodService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.util.List;

@RestController
@RequestMapping("goods")
public class GoodController {

    @Autowired
    private GoodService goodService;

    /**
     * 分页查询
     *
     * @param page
     * @param rows
     * @param sortBy
     * @param desc
     * @param key
     * @param saleable
     * @return
     */
    @GetMapping("/spu/page")
    public PageResult<Spu> querySpuByPage(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "rows", defaultValue = "5") Integer rows,
            @RequestParam(value = "sortBy", required = false) String sortBy,
            @RequestParam(value = "desc", defaultValue = "false") Boolean desc,
            @RequestParam(value = "key", required = false) String key,
            @RequestParam(value = "saleable", defaultValue = "true") Boolean saleable) {
        //分页查询spu信息
        SpuQueryByPageParameter spuQueryByPageParameter = new SpuQueryByPageParameter(page, rows, sortBy, desc, key, saleable);
        PageResult<Spu> result = goodService.querySpuByPageAndSort(spuQueryByPageParameter);
        System.out.println("查询数据量：" + result.getTotal());
        return result;
    }

    /**
     * 根据spu商品id查询详情
     *
     * @param id
     * @return
     */
    @GetMapping("/spu/detail/{id}")
    public SpuDetail querySpuDetailBySpuId(@PathVariable("id") Long id) {
        return this.goodService.querySpuDetailBySpuId(id);

    }

    /**
     * 根据Spu的id查询其下所有的sku
     *
     * @param id
     * @return
     */
    @GetMapping("sku/list/{id}")
    public List<Sku> querySkuBySpuId(@PathVariable("id") Long id) {
        return this.goodService.querySkuBySpuId(id);

    }
    /**
     * 根据Spu的id查询其下所有的sku
     *
     * @param ids
     * @return
     */
    @GetMapping("sku/list/ids")
    public List<Sku> querySkuByIds(@RequestParam("id") List<Long> ids) {
        return this.goodService.querySkuByIds(ids);

    }

    /**
     * 根据id查询商品
     *
     * @param id
     * @return
     */
    @GetMapping("/spu/{id}")
    public Spu queryGoodById(@PathVariable("id") Long id) {
        return this.goodService.queryGoodById(id);
    }

    /**
     * 根据id查询sku
     *
     * @param id
     * @return
     */
    @GetMapping("/sku/{id}")
    public Sku querySkuById(@PathVariable("id") Long id) {
        return this.goodService.querySkuById(id);

    }

    /**
     * 保存商品
     * @param spu
     * @return
     */
    @PostMapping
    public void saveGoods(@RequestBody SpuBo spu){
        this.goodService.saveGoods(spu);

    }




}
