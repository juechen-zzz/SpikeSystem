package com.komorebi.redis;

public class SpikeUserKey extends BasePrefix{
    private static final int TOKEN_EXPIRT = 3600 * 24;

    private SpikeUserKey(int expireSeconds, String prefix) {
        super(expireSeconds, prefix);
    }

    public static SpikeUserKey token = new SpikeUserKey(TOKEN_EXPIRT, "tk");
    public static SpikeUserKey getById = new SpikeUserKey(0, "id");

}
