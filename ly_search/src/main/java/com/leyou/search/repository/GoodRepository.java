package com.leyou.search.repository;

import com.leyou.search.pojo.Good;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * @Author: 98050
 * Time: 2018-10-11 22:17
 * Feature:文档操作
 */
public interface GoodRepository extends ElasticsearchRepository<Good,Long> {
}
