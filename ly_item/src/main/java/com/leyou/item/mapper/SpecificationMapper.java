package com.leyou.item.mapper;

import com.leyou.item.pojo.Specification;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.common.Mapper;

/**
 * @author li
 */
@Component
@org.apache.ibatis.annotations.Mapper
public interface SpecificationMapper extends Mapper<Specification> {
}