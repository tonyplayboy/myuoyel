package com.leyou.search.controller;

import com.leyou.common.entity.PageResult;
import com.leyou.item.pojo.Spu;
import com.leyou.item.pojo.SpuDetail;
import com.leyou.search.client.ItemClient;
import com.leyou.search.pojo.Good;
import com.leyou.search.pojo.SearchRequest;
import com.leyou.search.repository.GoodRepository;
import com.leyou.search.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
public class SearchController {

    @Autowired
    private GoodRepository goodRepository;
    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;
    @Autowired
    private ItemClient itemClient;
    @Autowired
    private SearchService searchService;

    /**
     * 普通搜索
     * @param searchRequest
     * @return
     */
    @PostMapping("page")
    public PageResult<Good> search(@RequestBody SearchRequest searchRequest) {
        return this.searchService.search(searchRequest);

    }


}
