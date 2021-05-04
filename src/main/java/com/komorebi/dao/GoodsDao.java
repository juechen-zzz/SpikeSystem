package com.komorebi.dao;

import com.komorebi.pojo.SpikeGoods;
import com.komorebi.vo.GoodsVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface GoodsDao {
    @Select("select g.*, sg.spike_price, sg.stock_count, sg.start_date, sg.end_date from spike_goods sg left join goods g on sg.goods_id = g.id")
    public List<GoodsVo> listGoodsVo();

    @Select("select g.*, sg.spike_price, sg.stock_count, sg.start_date, sg.end_date from spike_goods sg left join goods g on sg.goods_id = g.id where g.id = #{goodsId}")
    public GoodsVo getGoodsVoByGoodsId(@Param("goodsId") long goodsId);

    @Update("update spike_goods set stock_count = stock_count - 1 where goods_id = #{goodsId} and stock_count > 0")
    public int reduceStock(SpikeGoods g);

    @Update("update spike_goods set stock_count = #{stockCount} where goods_id = #{goodsId}")
    public int resetStock(SpikeGoods g);
}
