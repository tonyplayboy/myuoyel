package com.leyou.goods.controller;


import com.leyou.goods.service.GoodsHtmlService;
import com.leyou.goods.service.GoodsService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.thymeleaf.context.WebContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Controller
//不是Rest因为不想返回json数据
@RequestMapping("item")
public class GoodsController {

    @Autowired
    private GoodsService goodsService;
    @Autowired
    private GoodsHtmlService goodsHtmlService;

    @GetMapping(value = "{id}.html", produces = "text/html")
    public String toItemPage(Model model, @PathVariable("id") String id) {
        Long idN = Long.parseLong(id);
        //加载数据
        Map<String, Object> modelMap = goodsService.loadModel(idN);
        //把数据放入模型中
        model.addAllAttributes(modelMap);
        //页面静态化
        this.goodsHtmlService.asyncExecute(idN);
//        return "item";
//        BoundHashOperations<String, Object, Object> hashOperations = this.stringRedisTemplate.boundHashOps(KEY_PREFIX + id);
//        String html = (String) hashOperations.get(id);
//        /**
//         * 先取缓存
//         */
//        if (StringUtils.isNotEmpty(html)) {
//            //不空，则返回
//            return html;
//        }
//        //手动渲染模板
//        WebContext context = new WebContext(request, response, request.getServletContext(), request.getLocale(), model.asMap());
//        html = thymeleafViewResolver.getTemplateEngine().process("item", context);
//        if (StringUtils.isNotEmpty(html)) {
//            //不空，放入缓存
//            //设置有效期60秒
//            hashOperations.put(id, html);
//            hashOperations.expire(60, TimeUnit.SECONDS);
//        }
        return "item";
    }
}
