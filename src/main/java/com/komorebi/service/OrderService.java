package com.komorebi.service;

import com.komorebi.dao.OrderDao;
import com.komorebi.pojo.OrderInfo;
import com.komorebi.pojo.SpikeOrder;
import com.komorebi.pojo.SpikeUser;
import com.komorebi.redis.OrderKey;
import com.komorebi.redis.RedisService;
import com.komorebi.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
@Service
public class OrderService {
    @Autowired
    OrderDao orderDao;

    @Autowired
    RedisService redisService;

    public SpikeOrder getSpikeOrderByUserIdGoodsId(long userId, long goodsId) {
//        return orderDao.getSpikeOrderByUserIdGoodsId(userId, goodsId);
        return redisService.get(OrderKey.getSpikeOrderByUidGid, "" + userId + "_" + goodsId, SpikeOrder.class);
    }

    public OrderInfo getOrderById(long orderId) {
        return orderDao.getOrderById(orderId);
    }

    @Transactional
    public OrderInfo createOrder(SpikeUser user, GoodsVo goods) {
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setCreateDate(new Date());
        orderInfo.setDeliveryAddrId(0L);
        orderInfo.setGoodsCount(1);
        orderInfo.setGoodsId(goods.getId());
        orderInfo.setGoodsName(goods.getGoodsName());
        orderInfo.setGoodsPrice(goods.getSpikePrice());
        orderInfo.setOrderChannel(1);
        orderInfo.setStatus(0);
        orderInfo.setUserId(user.getId());
        orderDao.insert(orderInfo);

        SpikeOrder spikeOrder = new SpikeOrder();
        spikeOrder.setGoodsId(goods.getId());
        spikeOrder.setOrderId(orderInfo.getId());
        spikeOrder.setUserId(user.getId());
        orderDao.insertSpikeOrder(spikeOrder);

        redisService.set(OrderKey.getSpikeOrderByUidGid, "" + user.getId() + "_" + goods.getId(), spikeOrder);
        return orderInfo;
    }

    public void deleteOrders() {
        orderDao.deleteOrders();
        orderDao.deleteSpikeOrders();
    }
}
