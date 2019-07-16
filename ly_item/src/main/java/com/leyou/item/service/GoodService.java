package com.leyou.item.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.entity.PageResult;
import com.leyou.common.pojo.SpuQueryByPageParameter;
import com.leyou.item.mapper.*;
import com.leyou.item.pojo.Spu;
import com.leyou.item.pojo.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author: 98050
 * Time: 2018-08-14 22:15
 * Feature:
 */
@Service
public class GoodService {

    @Autowired
    private BrandMapper brandMapper;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private SpuMapper spuMapper;

    @Autowired
    private SpuDetailMapper spuDetailMapper;

    @Autowired
    private SkuMapper skuMapper;

    @Autowired
    private StockMapper stockMapper;
    @Autowired
    private AmqpTemplate amqpTemplate;

    private static final Logger LOGGER = LoggerFactory.getLogger(GoodService.class);

    /**
     * 分页查询
     *
     * @param spuQueryByPageParameter
     * @return
     */

    public PageResult<Spu> querySpuByPageAndSort(SpuQueryByPageParameter spuQueryByPageParameter) {

        //1.查询spu，分页查询，最多查询100条
        PageHelper.startPage(spuQueryByPageParameter.getPage(), Math.min(spuQueryByPageParameter.getRows(), 100));

        //2.创建查询条件
        Example example = new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();

        //3.条件过滤
        //3.1 是否过滤上下架
        if (spuQueryByPageParameter.getSaleable() != null) {
            System.out.println(spuQueryByPageParameter.getSaleable());
            criteria.orEqualTo("saleable", spuQueryByPageParameter.getSaleable());
        }
        //3.2 是否模糊查询
        if (StringUtils.isNotBlank(spuQueryByPageParameter.getKey())) {
            criteria.andLike("title", "%" + spuQueryByPageParameter.getKey() + "%");
        }
        //3.3 是否排序
        if (StringUtils.isNotBlank(spuQueryByPageParameter.getSortBy())) {
            example.setOrderByClause(spuQueryByPageParameter.getSortBy() + (spuQueryByPageParameter.getDesc() ? " DESC" : " ASC"));
        }
        PageInfo<Spu> pageInfo = new PageInfo<>(spuMapper.selectByExample(example));


        //将spu变为spubo
//        List<Spu> list = pageInfo.getResult().stream().map(spu -> {
//            Spu spu = new Spu();
//            //1.属性拷贝
//            BeanUtils.copyProperties(spu,spuBo);
//
//            //2.查询spu的商品分类名称，各级分类
//            List<String> nameList = this.categoryService.queryNameByIds(Arrays.asList(spu.getCid1(),spu.getCid2(),spu.getCid3()));
//            //3.拼接名字,并存入
//            spu.setCname(StringUtils.join(nameList,"/"));
//            //4.查询品牌名称
//            Brand brand = this.brandMapper.selectByPrimaryKey(spu.getBrandId());
//            spu.setBname(brand.getName());
//            return spu;
//        }).collect(Collectors.toList());

        return new PageResult<>(pageInfo.getTotal(), pageInfo.getList());
    }

    public SpuDetail querySpuDetailBySpuId(long id) {
        return this.spuDetailMapper.selectByPrimaryKey(id);
    }



    /**
     * 根据id查询商品信息
     *
     * @param id
     * @return
     */
    public SpuBo queryGoodById(Long id) {
        /**
         * 第一页所需信息如下：
         * 1.商品的分类信息、所属品牌、商品标题、商品卖点（子标题）
         * 2.商品的包装清单、售后服务
         */
        Spu spu = this.spuMapper.selectByPrimaryKey(id);
        SpuDetail spuDetail = this.spuDetailMapper.selectByPrimaryKey(spu.getId());

        Example example = new Example(Sku.class);
        example.createCriteria().andEqualTo("spuId", spu.getId());
        List<Sku> skuList = this.skuMapper.selectByExample(example);
        List<Long> skuIdList = new ArrayList<>();
        for (Sku sku : skuList) {
            skuIdList.add(sku.getId());
        }

        List<Stock> stocks = this.stockMapper.selectByIdList(skuIdList);

        for (Sku sku : skuList) {
            for (Stock stock : stocks) {
                if (sku.getId().equals(stock.getSkuId())) {
                    sku.setStock(stock.getStock());
                }
            }
        }

        SpuBo spuBo = new SpuBo(
                spu.getBrandId(), spu.getCid1(), spu.getCid2(), spu.getCid3(),
                spu.getTitle(), spu.getSubTitle(), spu.getSaleable(), spu.getValid(),
                spu.getCreateTime(), spu.getLastUpdateTime());
        spuBo.setSpuDetail(spuDetail);
        spuBo.setSkus(skuList);
        return spuBo;
    }

    public List<Sku> querySkuBySpuId(Long id) {
        Example example = new Example(Sku.class);
        example.createCriteria().andEqualTo("spuId", id);
        List<Sku> skuList = this.skuMapper.selectByExample(example);
        for (Sku sku : skuList) {
            Example temp = new Example(Stock.class);
            temp.createCriteria().andEqualTo("skuId", sku.getId());
            Stock stock = this.stockMapper.selectByExample(temp).get(0);
            sku.setStock(stock.getStock());
        }
        return skuList;
    }


    /**
     * 根据skuId查询sku
     *
     * @param id
     * @return
     */
    public Sku querySkuById(Long id) {
        return this.skuMapper.selectByPrimaryKey(id);
    }
    /**
     * 根据skuId集合查询sku
     *
     * @param ids
     * @return
     */
    public List<Sku> querySkuByIds(List<Long> ids) {
        return this.skuMapper.selectByIdList(ids);
    }

    /**
     * 保存商品
     * @param spu
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveGoods(SpuBo spu) {
        //保存spu
        spu.setSaleable(true);
        spu.setValid(true);
        spu.setCreateTime(new Date());
        spu.setLastUpdateTime(spu.getCreateTime());
        this.spuMapper.insert(spu);

        //保存spu详情
        SpuDetail spuDetail = spu.getSpuDetail();
        spuDetail.setSpuId(spu.getId());
        System.out.println(spuDetail.getSpecifications().length());
        this.spuDetailMapper.insert(spuDetail);

        //保存sku和库存信息
        saveSkuAndStock(spu.getSkus(),spu.getId());

        //发送消息到mq
        this.sendMessage(spu.getId(),"insert");
    }

    private void saveSkuAndStock(List<Sku> skus, Long id) {
        for (Sku sku : skus){
            if (!sku.getEnable()){
                continue;
            }
            //保存sku
            sku.setSpuId(id);
            //默认不参加任何促销
            sku.setCreateTime(new Date());
            sku.setLastUpdateTime(sku.getCreateTime());
            this.skuMapper.insert(sku);

            //保存库存信息
            Stock stock = new Stock();
            stock.setSkuId(sku.getId());
            stock.setStock(sku.getStock());
            this.stockMapper.insert(stock);
        }
    }

    /**
     * 发送消息到mq，生产者
     * @param id
     * @param type
     */
    public void sendMessage(Long id, String type) {
        try {
            this.amqpTemplate.convertAndSend("item." + type, id);
        }catch (Exception e){
            LOGGER.error("{}商品消息发送异常，商品id：{}",type,id,e);
        }
    }


}

