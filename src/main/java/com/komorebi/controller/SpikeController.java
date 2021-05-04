package com.komorebi.controller;

import com.komorebi.access.AccessLimit;
import com.komorebi.pojo.OrderInfo;
import com.komorebi.pojo.SpikeOrder;
import com.komorebi.pojo.SpikeUser;
import com.komorebi.rabbitmq.MQSender;
import com.komorebi.rabbitmq.SpikeMessage;
import com.komorebi.redis.*;
import com.komorebi.result.CodeMsg;
import com.komorebi.result.Result;
import com.komorebi.service.GoodsService;
import com.komorebi.service.OrderService;
import com.komorebi.service.SpikeService;
import com.komorebi.service.SpikeUserService;
import com.komorebi.util.MD5Util;
import com.komorebi.util.UUIDUtil;
import com.komorebi.vo.GoodsVo;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/spike")
public class SpikeController implements InitializingBean {
    @Autowired
    GoodsService goodsService;

    @Autowired
    OrderService orderService;

    @Autowired
    SpikeService spikeService;

    @Autowired
    RedisService redisService;

    @Autowired
    MQSender sender;

    private Map<Long, Boolean> localOverMap = new HashMap<Long, Boolean>();

    // 1 系统初始化，把商品库存加载到Redis
    @Override
    public void afterPropertiesSet() throws Exception {
        List<GoodsVo> goodsVoList = goodsService.listGoodsVo();
        if (goodsVoList == null) {return;}
        for (GoodsVo goodsVo : goodsVoList) {
            redisService.set(GoodsKey.getSpikeGoodsStock, "" + goodsVo.getId(), goodsVo.getStockCount());
            localOverMap.put(goodsVo.getId(), false);
        }
    }

    @RequestMapping(value = "/{path}/do_spike", method = RequestMethod.POST)
    @ResponseBody
    public Result<Integer> spike(Model model, SpikeUser user, @RequestParam("goodsId")long goodsId, @PathVariable("path") String path) {
        model.addAttribute("user", user);
        if (user == null) {return Result.error(CodeMsg.SERVER_ERROR);}

        // 验证path
        boolean check = spikeService.checkPath(user, goodsId, path);
        if (!check) {
            return Result.error(CodeMsg.REQUEST_ILLEGAL);
        }

        Boolean over = localOverMap.get(goodsId);
        if (over) {
            return Result.error(CodeMsg.SPIKE_OVER);
        }

        // 2.1 收到请求，Redis预减库存，库存不足，直接返回，否则进入3
        long stock = redisService.decr(GoodsKey.getSpikeGoodsStock, "" + goodsId);
        if (stock < 0) {
            localOverMap.put(goodsId, true);
            return Result.error(CodeMsg.SPIKE_OVER);}

        // 2.2 判定是否已经秒杀到了
        SpikeOrder order = orderService.getSpikeOrderByUserIdGoodsId(user.getId(), goodsId);
        if (order != null) {return Result.error(CodeMsg.REPEAT_SPIKE);}

        // 3 入队
        SpikeMessage spikeMessage = new SpikeMessage();
        spikeMessage.setUser(user);
        spikeMessage.setGoodsId(goodsId);
        sender.sendSpikeMessage(spikeMessage);
        return Result.success(0);

        /*
        // 判定库存
        GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
        int stock = goods.getStockCount();
        if (stock <= 0) { return Result.error(CodeMsg.SPIKE_OVER); }

        // 判定是否已经秒杀到了
        SpikeOrder order = orderService.getSpikeOrderByUserIdGoodsId(user.getId(), goodsId);
        if (order != null) {return Result.error(CodeMsg.REPEAT_SPIKE);}

        // 减库存，下订单，写入秒杀订单
        OrderInfo orderInfo = spikeService.spike(user, goods);
        return Result.success(orderInfo);

         */
    }

    // orderId：成功  -1: 秒杀失败  0：排队中
    @RequestMapping(value = "/result", method = RequestMethod.GET)
    @ResponseBody
    public Result<Long> spikeResult(Model model, SpikeUser user, @RequestParam("goodsId")long goodsId) {
        model.addAttribute("user", user);
        if (user == null) {
            return Result.error(CodeMsg.SERVER_ERROR);
        }

        long result = spikeService.getSpikeResult(user.getId(), goodsId);
        return Result.success(result);
    }

    // 数据库重置秒杀订单
    @RequestMapping(value="/reset", method=RequestMethod.GET)
    @ResponseBody
    public Result<Boolean> reset(Model model) {
        List<GoodsVo> goodsList = goodsService.listGoodsVo();
        for(GoodsVo goods : goodsList) {
            goods.setStockCount(10);
            redisService.set(GoodsKey.getSpikeGoodsStock, ""+goods.getId(), 10);
            localOverMap.put(goods.getId(), false);
        }
        redisService.delete(OrderKey.getSpikeOrderByUidGid);
        redisService.delete(SpikeKey.isGoodsOver);
        spikeService.reset(goodsList);
        return Result.success(true);
    }

    // 秒杀地址隐藏
    @AccessLimit(seconds=5,maxCount=5,needLogin=true)
    @RequestMapping(value="/path", method=RequestMethod.GET)
    @ResponseBody
    public Result<String> getSpikePath(HttpServletRequest request, Model model, SpikeUser user, @RequestParam("goodsId")long goodsId, @RequestParam("verifyCode")int verifyCode) {
        model.addAttribute("user", user);
        if (user == null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }

        // 验证码校验
        boolean check = spikeService.checkVerifyCode(user, goodsId, verifyCode);
        if (!check) {return Result.error(CodeMsg.REQUEST_ILLEGAL);}

        String path = spikeService.createSpikePath(user, goodsId);
        return Result.success(path);
    }

    // 生成图片验证码接口
    @RequestMapping(value="/verifyCode", method=RequestMethod.GET)
    @ResponseBody
    public Result<String> getSpikeVerifyCode(HttpServletResponse response, SpikeUser user, long goodsId){
        if (user == null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        BufferedImage image = spikeService.createVerifyCode(user, goodsId);
        try {
            OutputStream out = response.getOutputStream();
            ImageIO.write(image, "JPEG", out);
            out.flush();
            out.close();
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(CodeMsg.SPIKE_FAIL);
        }
    }
}
