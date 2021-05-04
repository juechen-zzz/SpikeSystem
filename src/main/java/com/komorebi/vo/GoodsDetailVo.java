package com.komorebi.vo;

import com.komorebi.pojo.SpikeUser;

public class GoodsDetailVo {
    private int spikeStatus = 0;
    private int remainSeconds = 0;
    private GoodsVo goodsVo;
    private SpikeUser user;

    public int getSpikeStatus() {
        return spikeStatus;
    }

    public void setSpikeStatus(int spikeStatus) {
        this.spikeStatus = spikeStatus;
    }

    public int getRemainSeconds() {
        return remainSeconds;
    }

    public void setRemainSeconds(int remainSeconds) {
        this.remainSeconds = remainSeconds;
    }

    public GoodsVo getGoodsVo() {
        return goodsVo;
    }

    public void setGoodsVo(GoodsVo goodsVo) {
        this.goodsVo = goodsVo;
    }

    public SpikeUser getUser() {
        return user;
    }

    public void setUser(SpikeUser user) {
        this.user = user;
    }
}
