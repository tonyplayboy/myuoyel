package com.leyou.search.test;
import com.leyou.common.entity.PageResult;
import com.leyou.item.pojo.Spu;
import com.leyou.item.pojo.SpuDetail;
import com.leyou.search.SearchApplication;
import com.leyou.search.client.ItemClient;
import com.leyou.search.pojo.Good;
import com.leyou.search.repository.GoodRepository;
import com.leyou.search.service.SearchService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = SearchApplication.class)
public class GoodRepositoryTest {
    @Autowired
    private GoodRepository goodRepository;
    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;
    @Autowired
    private ItemClient itemClient;
    @Autowired
    private SearchService searchService;


    @Test
    public void testCreateIndex() {
        elasticsearchTemplate.createIndex(Good.class);
        elasticsearchTemplate.putMapping(Good.class);
    }




    @Test
    public void loadData() throws IOException {
        List<Spu> list = new ArrayList<>();
        int page = 1;
        int row = 100;
        int size;
        do {
            //分页查询数据
            PageResult<Spu> result = itemClient.querySpuByPage(page, row, null, true, null, true);
            List<Spu> spus = result.getItems();
            size = spus.size();
            page ++;
            list.addAll(spus);
        }while (size == 100);

        //创建Goods集合
        List<Good> goodsList = new ArrayList<>();
        //遍历spu
        for (Spu spu : list) {
            try {
                System.out.println("spu id" + spu.getId());
                Good Good = this.searchService.buildGood(spu);
                goodsList.add(Good);
            } catch (IOException e) {
                System.out.println("查询失败：" + spu.getId());
                throw e;
            }
        }
        //elasticsearch方法
        this.goodRepository.saveAll(goodsList);
    }

}