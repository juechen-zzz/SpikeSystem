package com.komorebi.redis;

// 前缀实现接口
public interface KeyPrefix {
    public int expireSeconds();

    public String getPrefix();
}
