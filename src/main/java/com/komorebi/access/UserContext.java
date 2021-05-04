package com.komorebi.access;

import com.komorebi.pojo.SpikeUser;

public class UserContext {
    private static ThreadLocal<SpikeUser> userHolder = new ThreadLocal<SpikeUser>();

    public static void setUser(SpikeUser user) {
        userHolder.set(user);
    }

    public static SpikeUser getUser() {
        return userHolder.get();
    }
}
