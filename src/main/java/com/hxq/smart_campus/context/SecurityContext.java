package com.hxq.smart_campus.context;

import com.hxq.smart_campus.entity.info.LoginUserInfo;

public class SecurityContext {

    private static final ThreadLocal<LoginUserInfo> USER_INFO_HOLDER = new ThreadLocal<>();

    private SecurityContext() {}

    public static void setUserInfo(LoginUserInfo userInfo) {
        USER_INFO_HOLDER.set(userInfo);
    }

    public static LoginUserInfo getUserInfo() {
        return USER_INFO_HOLDER.get();
    }

    public static void clear() {
        USER_INFO_HOLDER.remove();
    }

    public static Long getCurrentUserId() {
        LoginUserInfo userInfo = getUserInfo();
        return userInfo != null ? userInfo.getUserId() : null;
    }

    public static String getCurrentUsername() {
        LoginUserInfo userInfo = getUserInfo();
        return userInfo != null ? userInfo.getUsername() : null;
    }

    public static String getCurrentUserName() {
        LoginUserInfo userInfo = getUserInfo();
        return userInfo != null ? userInfo.getName() : null;
    }

    public static String getCurrentUserType() {
        LoginUserInfo userInfo = getUserInfo();
        return userInfo != null ? userInfo.getUserType() : null;
    }
}