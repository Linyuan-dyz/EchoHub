package com.nowcoder.community.common.utils;

import com.nowcoder.community.domain.po.User;

public class UserInfoHolder {

    private static final ThreadLocal<User> userThreadLocal = new ThreadLocal<>();
    private static final ThreadLocal<String> refreshTokenThreadLocal = new ThreadLocal<>();

    public static void setUserInfo(User user) {
        userThreadLocal.set(user);
    }

    public static void setRefreshToken(String refreshToken) {
        refreshTokenThreadLocal.set(refreshToken);
    }

    public static String getRefreshToken() {
        return refreshTokenThreadLocal.get();
    }

    public static User getUser() {
        return userThreadLocal.get() == null ? null : userThreadLocal.get();
    }

    public static void removeUser() {
        userThreadLocal.remove();
    }
}
