package com.leyou.item.mapper;

import com.leyou.item.pojo.Spu;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.common.Mapper;

/**
 * @Author: 98050
 * Time: 2018-08-14 22:14
 * Feature:
 */
@Component
@org.apache.ibatis.annotations.Mapper
public interface SpuMapper extends Mapper<Spu> {
}
