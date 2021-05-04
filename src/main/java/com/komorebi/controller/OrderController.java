package com.komorebi.controller;

import com.komorebi.pojo.OrderInfo;
import com.komorebi.pojo.SpikeUser;
import com.komorebi.result.CodeMsg;
import com.komorebi.result.Result;
import com.komorebi.service.GoodsService;
import com.komorebi.service.OrderService;
import com.komorebi.vo.GoodsVo;
import com.komorebi.vo.OrderDetailVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/order")
public class OrderController {
    @Autowired
    OrderService orderService;

    @Autowired
    GoodsService goodsService;

    @RequestMapping("/detail")
    @ResponseBody
    public Result<OrderDetailVo> info(SpikeUser user, @RequestParam("orderId") long orderId) {
        if (user == null) {return Result.error(CodeMsg.SESSION_ERROR);}
        OrderInfo order = orderService.getOrderById(orderId);
        if (order == null) {
            return Result.error(CodeMsg.ORDER_NOT_EXIST);
        }
        Long goodsId = order.getGoodsId();
        GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);

        OrderDetailVo vo = new OrderDetailVo();
        vo.setGoodsVo(goods);
        vo.setOrderInfo(order);
        return Result.success(vo);
    }
}
