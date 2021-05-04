package com.komorebi.controller;

import com.komorebi.pojo.SpikeUser;
import com.komorebi.redis.GoodsKey;
import com.komorebi.redis.RedisService;
import com.komorebi.result.Result;
import com.komorebi.service.GoodsService;
import com.komorebi.service.SpikeUserService;
import com.komorebi.vo.GoodsDetailVo;
import com.komorebi.vo.GoodsVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.spring5.view.ThymeleafViewResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;


@Controller
@RequestMapping("/goods")
public class GoodsController {
    @Autowired
    SpikeUserService spikeUserService;

    @Autowired
    RedisService redisService;

    @Autowired
    GoodsService goodsService;

    @Autowired
    ThymeleafViewResolver thymeleafViewResolver;

    @RequestMapping(value = "/to_list", produces = "text/html")
    @ResponseBody
    public String list(HttpServletRequest request, HttpServletResponse response, Model model, SpikeUser user) {
        model.addAttribute("user", user);
        // 页面缓存1：取缓存
        String html = redisService.get(GoodsKey.getGoodsList, "", String.class);
        if (!StringUtils.isEmpty(html)) {
            return html;
        }
        
        // 查询商品列表
        List<GoodsVo> goodsList = goodsService.listGoodsVo();
        model.addAttribute("goodsList", goodsList);

//        return "goods_list";

        // 页面缓存2：手动渲染
        WebContext ctx = new WebContext(request, response, request.getServletContext(), request.getLocale(), model.asMap());
        html = thymeleafViewResolver.getTemplateEngine().process("goods_list", ctx);
        if (!StringUtils.isEmpty(html)) {
            redisService.set(GoodsKey.getGoodsList, "", html);
        }
        // 页面缓存3：结果输出
        return html;
    }

    @RequestMapping(value = "/to_detail/{goodsId}", produces = "text/html")
    @ResponseBody
    public String detail2(HttpServletRequest request, HttpServletResponse response, Model model, SpikeUser user, @PathVariable("goodsId")long goodsId) {
        model.addAttribute("user", user);

        // URL缓存1：取缓存
        String html = redisService.get(GoodsKey.getGoodsDetail, "" + goodsId, String.class);
        if (!StringUtils.isEmpty(html)) {
            return html;
        }

        // URL缓存2：手动渲染
        GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
        model.addAttribute("goods", goods);

        long startAt = goods.getStartDate().getTime();
        long endAt = goods.getEndDate().getTime();
        long now = System.currentTimeMillis();

        int spikeStatus = 0;
        int remainSeconds = 0;

        if (now < startAt) {            // 秒杀未开始
            spikeStatus = 0;
            remainSeconds = (int)(startAt - now) / 1000;
        }
        else if (now > endAt) {         // 秒杀已结束
            spikeStatus = 2;
            remainSeconds = -1;
        }
        else {                          // 秒杀进行中
            spikeStatus = 1;
            remainSeconds = 0;
        }
        model.addAttribute("spikeStatus", spikeStatus);
        model.addAttribute("remainSeconds", remainSeconds);
//        return "goods_detail";

        WebContext ctx = new WebContext(request, response, request.getServletContext(), request.getLocale(), model.asMap());
        html = thymeleafViewResolver.getTemplateEngine().process("goods_detail", ctx);
        if (!StringUtils.isEmpty(html)) {
            redisService.set(GoodsKey.getGoodsDetail, "" + goodsId, html);
        }
        // URL缓存3：结果输出
        return html;
    }

    // 页面静态化
    @RequestMapping(value = "/detail/{goodsId}")
    @ResponseBody
    public Result<GoodsDetailVo> detail(HttpServletRequest request, HttpServletResponse response, Model model, SpikeUser user, @PathVariable("goodsId")long goodsId) {
        GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);

        long startAt = goods.getStartDate().getTime();
        long endAt = goods.getEndDate().getTime();
        long now = System.currentTimeMillis();

        int spikeStatus = 0;
        int remainSeconds = 0;

        if (now < startAt) {            // 秒杀未开始
            spikeStatus = 0;
            remainSeconds = (int)(startAt - now) / 1000;
        }
        else if (now > endAt) {         // 秒杀已结束
            spikeStatus = 2;
            remainSeconds = -1;
        }
        else {                          // 秒杀进行中
            spikeStatus = 1;
            remainSeconds = 0;
        }
        GoodsDetailVo vo = new GoodsDetailVo();
        vo.setGoodsVo(goods);
        vo.setUser(user);
        vo.setRemainSeconds(remainSeconds);
        vo.setSpikeStatus(spikeStatus);
        return Result.success(vo);
    }
}
