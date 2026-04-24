package com.hxq.smart_campus.utils;


import lombok.extern.slf4j.Slf4j;

/**
 * 敏感信息脱敏工具类
 * @author hxq
 * @description 敏感信息脱敏工具类，用于脱敏用户敏感信息
 */
@Slf4j
public class SensitiveInfoUtils {

    /**
     * 对密码进行脱敏处理
     * @param password 原始密码
     * @return 脱敏后的密码
     */
    public static String maskPassword(String password) {
        if (password == null || password.isEmpty()) {
            return "";
        }
        return "******";
    }

    /**
     * 对手机号进行脱敏处理
     * @param phone 原始手机号
     * @return 脱敏后的手机号
     */
    public static String maskPhone(String phone) {
        if (phone == null || phone.length() < 11) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }

    /**
     * 对身份证号进行脱敏处理
     * @param idCard 原始身份证号
     * @return 脱敏后的身份证号
     */
    public static String maskIdCard(String idCard) {
        if (idCard == null || idCard.length() < 18) {
            return idCard;
        }
        return idCard.substring(0, 3) + "***********" + idCard.substring(14);
    }
}
