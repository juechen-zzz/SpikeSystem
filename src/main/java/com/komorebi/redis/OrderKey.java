package com.komorebi.redis;

public class OrderKey extends BasePrefix {
    public OrderKey(String prefix) {
        super(prefix);
    }
    public static OrderKey getSpikeOrderByUidGid = new OrderKey("soug");
}
