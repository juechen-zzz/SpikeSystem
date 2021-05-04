package com.komorebi.rabbitmq;

import com.komorebi.pojo.OrderInfo;
import com.komorebi.pojo.SpikeOrder;
import com.komorebi.pojo.SpikeUser;
import com.komorebi.redis.RedisService;
import com.komorebi.result.CodeMsg;
import com.komorebi.result.Result;
import com.komorebi.service.GoodsService;
import com.komorebi.service.OrderService;
import com.komorebi.service.SpikeService;
import com.komorebi.vo.GoodsVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MQReveiver {
    @Autowired
    GoodsService goodsService;

    @Autowired
    OrderService orderService;

    @Autowired
    SpikeService spikeService;

    private static Logger log = LoggerFactory.getLogger(MQReveiver.class);

    @RabbitListener(queues = MQConfig.QUEUE)
    public void receive(String message) {
        log.info("receive message: " + message);
    }

    @RabbitListener(queues = MQConfig.TOPIC_QUEUE1)
    public void receiveTopic1(String message) {
        log.info("receive queue1 message: " + message);
    }

    @RabbitListener(queues = MQConfig.TOPIC_QUEUE2)
    public void receiveTopic2(String message) {
        log.info("receive queue2 message: " + message);
    }

    @RabbitListener(queues = MQConfig.HEADERS_QUEUE)
    public void receiveHeader(byte[] message) {
        log.info("receive header message: " + new String(message));
    }

    @RabbitListener(queues = MQConfig.SPIKE_QUEUE)
    public void receiveSpikeQueue(String message) {
        log.info("receive spike message: " + message);
        SpikeMessage spikeMessage = RedisService.stringToBean(message, SpikeMessage.class);
        SpikeUser spikeUser = spikeMessage.getUser();
        long goodsId = spikeMessage.getGoodsId();

        // 判定库存
        GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
        int stock = goods.getStockCount();
        if (stock <= 0) { return; }

        // 判定是否已经秒杀到了
        SpikeOrder order = orderService.getSpikeOrderByUserIdGoodsId(spikeUser.getId(), goodsId);
        if (order != null) {return;}

        // 减库存，下订单，写入秒杀订单
        spikeService.spike(spikeUser, goods);
    }
}
