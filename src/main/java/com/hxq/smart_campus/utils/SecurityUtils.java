package com.hxq.smart_campus.utils;

import com.hxq.smart_campus.context.SecurityContext;

public class SecurityUtils {

    private SecurityUtils() {}

    public static Long getCurrentUserId() {
        return SecurityContext.getCurrentUserId();
    }

    public static String getCurrentUserName() {
        return SecurityContext.getCurrentUserName();
    }

    public static String getCurrentUsername() {
        return SecurityContext.getCurrentUsername();
    }

    public static String getCurrentUserType() {
        return SecurityContext.getCurrentUserType();
    }
}