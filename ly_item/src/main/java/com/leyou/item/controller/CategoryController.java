package com.leyou.item.controller;


import com.leyou.common.entity.Result;
import com.leyou.common.enums.BaseExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.item.pojo.Category;
import com.leyou.item.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping("category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;


    @GetMapping("list")
    public List<Category> queryListByPid(@RequestParam Long pid) {
        return categoryService.queryListByPid(pid);

    }

    /**
     * 根据分类id集合查询分类名称
     * @param ids
     * @return
     */
    @GetMapping("names")
    public List<String> queryNameByIds(@RequestParam("ids")List<Long> ids){
        return categoryService.queryNameByIds(ids);

    }

    @GetMapping("all")
    public List<Category> queryCategoryByIds(@RequestParam("ids") List<Long> ids) {
        return categoryService.queryByIds(ids);
    }


}
