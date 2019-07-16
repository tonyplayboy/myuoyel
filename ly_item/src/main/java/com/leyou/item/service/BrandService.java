package com.leyou.item.service;

import com.github.pagehelper.PageHelper;
import com.leyou.item.mapper.BrandMapper;
import com.leyou.item.pojo.Brand;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service

public class BrandService {
    @Autowired
    private BrandMapper brandMapper;

    public List<Brand> queryBrandByPage(Integer page, Integer rows, String sortBy, Boolean desc, String key) {
        //分页
        PageHelper.startPage(page, rows);
        //按条件搜索
        /**
         * where `name` like "%x%" or letter == "x"
         * order by id desc
         */
        Example example = new Example(Brand.class);
        if (StringUtils.isNotBlank(key)) {
            example.createCriteria()
                    .orLike("name", "%" + key + "%")
                    .orEqualTo("letter", key.toUpperCase());
        }
        if (StringUtils.isNotBlank(sortBy)) {
            String orderByClause = sortBy + " " + (desc ? "desc" : "asc");
            example.setOrderByClause(orderByClause);
        }
        return brandMapper.selectByExample(example);
    }
    @Transactional
    public void saveBrand(Brand brand, List<Long> cids) {
        brandMapper.insert(brand);
        for (Long cid : cids) {
            brandMapper.insertCategoryBrand(cid, brand.getId());
        }


    }

    public Brand queryBrandById(Long id) {
        return brandMapper.selectByPrimaryKey(id);
    }

    /**
     * 根据品牌id集合查询品牌信息
     * @param ids
     * @return
     */
    public List<Brand> queryBrandByBrandIds(List<Long> ids) {
        return this.brandMapper.selectByIdList(ids);
    }
}
