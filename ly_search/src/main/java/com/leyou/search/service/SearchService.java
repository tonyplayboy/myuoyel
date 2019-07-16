package com.leyou.search.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.leyou.common.entity.PageResult;
import com.leyou.common.utils.JsonUtils;
import com.leyou.common.utils.NumberUtils;
import com.leyou.search.client.ItemClient;
import com.leyou.item.pojo.*;
import com.leyou.search.pojo.Good;
import com.leyou.search.pojo.SearchRequest;
import com.leyou.search.pojo.SearchResult;
import com.leyou.search.repository.GoodRepository;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.histogram.InternalHistogram;
import org.elasticsearch.search.aggregations.bucket.terms.LongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.aggregations.metrics.stats.InternalStats;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Author: 98050
 * Time: 2018-10-11 22:59
 * Feature: 搜索功能
 */
@Service
public class SearchService {

    @Autowired
    private ItemClient itemClient;


    @Autowired
    private GoodRepository goodRepository;

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    private ObjectMapper mapper = new ObjectMapper();

    /**
     * 查询商品信息
     * @param spu
     * @return
     * @throws IOException
     */
    @Transactional
    public Good buildGood(Spu spu) throws IOException {
        Good Good = new Good();

        //1.查询商品分类名称
        List<String> categoryNames = this.itemClient.queryNameByIds(
                Arrays.asList(spu.getCid1(),spu.getCid2(),spu.getCid3()));
        //2. 查询品牌
        Brand brand = itemClient.queryBrandById(spu.getBrandId());
        //2.查询sku
        List<Sku> skus = itemClient.querySkuBySpuId(spu.getId());
        //3.查询详情
        SpuDetail spuDetail = itemClient.querySpuDetailBySpuId(spu.getId());

        //4.处理sku,仅封装id，价格、标题、图片、并获得价格集合
        List<Long> prices = new ArrayList<>();
        List<Map<String,Object>> skuLists = new ArrayList<>();
        skus.forEach(sku -> {
            prices.add(sku.getPrice());
            Map<String,Object> skuMap = new HashMap<>();
            skuMap.put("id",sku.getId());
            skuMap.put("title",sku.getTitle());
            skuMap.put("price",sku.getPrice());
            //取第一张图片
            skuMap.put("image", StringUtils.isBlank(sku.getImages()) ? "" : StringUtils.split(sku.getImages(),",")[0]);
            skuLists.add(skuMap);
        });

        //提取公共属性
        List<Map<String,Object>> genericSpecs = mapper.readValue(spuDetail.getSpecifications(),new TypeReference<List<Map<String,Object>>>(){});
        //提取特有属性
        Map<String,Object> specialSpecs = mapper.readValue(spuDetail.getSpecTemplate(),new TypeReference<Map<String,Object>>(){});

        //过滤规格模板，把所有可搜索的信息保存到Map中,key是规格参数的名字,value是规格参数的值
        Map<String,Object> specMap = new HashMap<>();

        String searchable = "searchable";
        String v = "v";
        String k = "k";
        String options = "options";

        genericSpecs.forEach(m -> {
            List<Map<String, Object>> params = (List<Map<String, Object>>) m.get("params");
            params.forEach(spe ->{
                if ((boolean)spe.get(searchable)){
                    if (spe.get(v) != null){
                        specMap.put(spe.get(k).toString(), spe.get(v));
                    }else if (spe.get(options) != null){
                        specMap.put(spe.get(k).toString(), spe.get(options));
                    }
                }
            });
        });

        Good.setSubTitle(spu.getSubTitle());
        Good.setId(spu.getId());
        Good.setBrandId(spu.getBrandId());
        Good.setCid1(spu.getCid1());
        Good.setCid2(spu.getCid2());
        Good.setCid3(spu.getCid3());
        Good.setCreateTime(spu.getCreateTime());
        //搜索字段,包含标题,分类,品牌,规格
        Good.setAll(spu.getTitle() + " " + StringUtils.join(categoryNames, " ") + " " + brand.getName());
        Good.setPrice(prices); //价格集合
        Good.setSkus(mapper.writeValueAsString(skuLists)); //所有sku的集合的json格式
        Good.setSpecs(specMap); //所有的可搜索的规格参数
        return Good;
    }

    /**
     * 搜索Elasticsearch
     * @param searchRequest
     * @return
     */
    public PageResult<Good> search(SearchRequest searchRequest) {
        String key = searchRequest.getKey();
        Integer page = searchRequest.getPage();
        Integer size = searchRequest.getSize();
        //判断是否有搜索条件，如果没有，直接返回null。不允许搜索全部商品
        if (StringUtils.isBlank(key)){
            return null;
        }
        //创建查询构建器
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();

        //构建分页和排序
        queryBuilder.withPageable(PageRequest.of(page - 1, size));
        //构建关键字
        MatchQueryBuilder basicQuery = QueryBuilders.matchQuery("all", key);
        queryBuilder.withQuery(basicQuery);
        //1.2.通过sourceFilter设置返回的结果字段，只需要id,skus,subTitle
        queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{"id","skus","subTitle"},null));

        //1.3.聚合
        //商品分类聚合名称
        String categoryAggName = "category";
        //品牌聚合名称
        String brandAggName = "brand";
        //1.3.1。对商品分类进行聚合
        queryBuilder.addAggregation(AggregationBuilders.terms(categoryAggName).field("cid3"));
        //1.3.2.对品牌进行聚合
        queryBuilder.addAggregation(AggregationBuilders.terms(brandAggName).field("brandId"));

        //查询
        //Page<Good> pageInfo = goodRepository.search(queryBuilder.build());
        AggregatedPage<Good> pageInfo = elasticsearchTemplate.queryForPage(queryBuilder.build(), Good.class);
        //解析查询结果
        //分页信息
        long total = pageInfo.getTotalElements();
        long totalPage = pageInfo.getTotalPages();
        List<Good> goodList = pageInfo.getContent();


        Aggregation categoryAgg = pageInfo.getAggregations().get(categoryAggName);
        Aggregation brandAgg = pageInfo.getAggregations().get(brandAggName);
        //3.2 商品分类的聚合结果
        List<Category> categories = getCategoryAggResult(categoryAgg);
        //3.3 品牌的聚合结果
        List<Brand> brands = getBrandAggResult(brandAgg);
        //3.4 处理规格参数
        List<Map<String,Object>> specs = null;
        if (categories.size() == 1){
            //如果商品分类只有一个进行聚合，并根据分类与基本查询条件聚合
            specs = getSpec(categories.get(0).getId(),basicQuery);
        }
        //4.封装结果，返回
        return new SearchResult<>(total, totalPage,goodList,categories,brands, specs);
    }

    /**
     * 解析商品分类聚合结果，其中都是三级分类
     * @param aggregation
     * @return
     */
    private List<Category> getCategoryAggResult(Aggregation aggregation) {
        LongTerms brandAgg = (LongTerms) aggregation;
        List<Long> cids = new ArrayList<>();
        for (LongTerms.Bucket bucket : brandAgg.getBuckets()){
            cids.add(bucket.getKeyAsNumber().longValue());
        }
        //根据id查询分类名称
        return itemClient.queryCategoryByIds(cids);
    }

    /**
     * 解析品牌聚合结果
     * @param aggregation
     * @return
     */
    private List<Brand> getBrandAggResult(Aggregation aggregation) {
        LongTerms brandAgg = (LongTerms) aggregation;
        List<Long> bids = new ArrayList<>();
        for (LongTerms.Bucket bucket : brandAgg.getBuckets()){
            bids.add(bucket.getKeyAsNumber().longValue());
        }
        //根据品牌id查询品牌
        return itemClient.queryBrandByIds(bids);
    }

    /**
     * 聚合规格参数
     *
     */
    private List<Map<String, Object>> getSpec(Long cid3, QueryBuilder basicQuery) {
        //1.将规格反序列化为集合
        List<Map<String,Object>> specs = null;
        //不管是全局参数还是sku参数，只要是搜索参数，都根据分类id查询出来
        String specsJSONStr = itemClient.querySpecificationByCategoryId(cid3);
        specs = JsonUtils.nativeRead(specsJSONStr, new TypeReference<List<Map<String, Object>>>() {
        });
        //2.过滤出可以搜索的规格参数名称，分成数值类型、字符串类型
        Set<String> strSpec = new HashSet<>();
        //准备map，用来保存数值规格参数名及单位
        Map<String,String> numericalUnits = new HashMap<>();
        //解析规格
        String searchable = "searchable";
        String numerical = "numerical";
        String k = "k";
        String unit = "unit";
        for (Map<String,Object> spec :specs){
            List<Map<String, Object>> params = (List<Map<String, Object>>) spec.get("params");
            params.forEach(param ->{
                if ((boolean)param.get(searchable)){
                    if (param.containsKey(numerical) && (boolean)param.get(numerical)){
                        numericalUnits.put(param.get(k).toString(),param.get(unit).toString());
                    }else {
                        strSpec.add(param.get(k).toString());
                    }
                }
            });
        }
        //3.聚合计算数值类型的interval
        Map<String,Double> numericalInterval = getNumberInterval(cid3,numericalUnits.keySet());
        return this.aggForSpec(strSpec,numericalInterval,numericalUnits,basicQuery);
    }
    /**
     * 聚合得到interval
     * @param id
     * @param keySet
     * @return
     */
    private Map<String, Double> getNumberInterval(Long id, Set<String> keySet) {
        Map<String,Double> numbericalSpecs = new HashMap<>();
        //准备查询条件
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        //不查询任何数据
        queryBuilder.withQuery(QueryBuilders.termQuery("cid3",id.toString())).withSourceFilter(new FetchSourceFilter(new String[]{""},null)).withPageable(PageRequest.of(0,1));
        //添加stats类型的聚合,同时返回avg、max、min、sum、count等
        for (String key : keySet){
            queryBuilder.addAggregation(AggregationBuilders.stats(key).field("specs." + key));
        }
        Map<String,Aggregation> aggregationMap = this.elasticsearchTemplate.query(queryBuilder.build(),
                searchResponse -> searchResponse.getAggregations().asMap()
        );
        for (String key : keySet){
            InternalStats stats = (InternalStats) aggregationMap.get(key);
            double interval = this.getInterval(stats.getMin(),stats.getMax(),stats.getSum());
            numbericalSpecs.put(key,interval);
        }
        return numbericalSpecs;
    }

    /**
     * 根据最小值，最大值，sum计算interval
     * @param min
     * @param max
     * @param sum
     * @return
     */
    private double getInterval(double min, double max, Double sum) {
        //显示7个区间
        double interval = (max - min) / 6.0d;
        //判断是否是小数
        if (sum.intValue() == sum){
            //不是小数，要取整十、整百
            int length = StringUtils.substringBefore(String.valueOf(interval),".").length();
            double factor = Math.pow(10.0,length - 1);
            return Math.round(interval / factor)*factor;
        }else {
            //是小数的话就保留一位小数
            return NumberUtils.scale(interval,1);
        }
    }

    /**
     * 根据规格参数，聚合得到过滤属性值
     * @param strSpec
     * @param numericalInterval
     * @param numericalUnits
     * @param basicQuery
     * @return
     */
    private List<Map<String, Object>> aggForSpec(Set<String> strSpec, Map<String, Double> numericalInterval, Map<String, String> numericalUnits, QueryBuilder basicQuery) {
        List<Map<String,Object>> specs = new ArrayList<>();
        //准备查询条件
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        queryBuilder.withQuery(basicQuery);
        //聚合数值类型
        for (Map.Entry<String,Double> entry : numericalInterval.entrySet()) {
            queryBuilder.addAggregation(AggregationBuilders.histogram(entry.getKey()).field("specs." + entry.getKey()).interval(entry.getValue()).minDocCount(1));
        }
        //聚合字符串
        for (String key :strSpec){
            queryBuilder.addAggregation(AggregationBuilders.terms(key).field("specs."+key+".keyword"));
        }

        //解析聚合结果
        Map<String,Aggregation> aggregationMap = this.elasticsearchTemplate.query(queryBuilder.build(), SearchResponse:: getAggregations).asMap();

        //解析数值类型
        for (Map.Entry<String,Double> entry :numericalInterval.entrySet()){
            Map<String,Object> spec = new HashMap<>();
            String key = entry.getKey();
            spec.put("k",key);
            spec.put("unit",numericalUnits.get(key));
            //获取聚合结果
            InternalHistogram histogram = (InternalHistogram) aggregationMap.get(key);
            spec.put("options",histogram.getBuckets().stream().map(bucket -> {
                Double begin = (Double) bucket.getKey();
                Double end = begin + numericalInterval.get(key);
                //对begin和end取整
                if (NumberUtils.isInt(begin) && NumberUtils.isInt(end)){
                    //确实是整数，直接取整
                    return begin.intValue() + "-" + end.intValue();
                }else {
                    //小数，取2位小数
                    begin = NumberUtils.scale(begin,2);
                    end = NumberUtils.scale(end,2);
                    return begin + "-" + end;
                }
            }).collect(Collectors.toList()));
            specs.add(spec);
        }

        //解析字符串类型
        strSpec.forEach(key -> {
            Map<String,Object> spec = new HashMap<>();
            spec.put("k",key);
            StringTerms terms = (StringTerms) aggregationMap.get(key);
            spec.put("options",terms.getBuckets().stream().map((Function<StringTerms.Bucket, Object>) StringTerms.Bucket::getKeyAsString).collect(Collectors.toList()));
            specs.add(spec);
        });
        return specs;
    }


    /**
     * 创建索引
     * @param id
     */
    public void createOrUpdateIndex(Long id) throws Exception{
        SpuBo spuBo = this.itemClient.queryGoodById(id);
        //构建商品
        Good good = buildGood(spuBo);

        //保存数据到索引库中
        this.goodRepository.save(good);
    }

    /**
     * 删除索引
     * @param id
     */
    public void deleteIndex(Long id) {

        goodRepository.deleteById(id);
    }


}
