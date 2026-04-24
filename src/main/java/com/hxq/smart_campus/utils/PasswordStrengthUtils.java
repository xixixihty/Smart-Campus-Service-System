package com.hxq.smart_campus.utils;

import java.util.regex.Pattern;

/**
 * 密码强度工具类
 * @author hxq
 * @description 密码强度工具类，用于验证密码强度
 */
public class PasswordStrengthUtils {

    // 密码长度要求：至少8位
    private static final int MIN_LENGTH = 8;
    // 包含数字的正则表达式
    private static final Pattern DIGIT_PATTERN = Pattern.compile("[0-9]");
    // 包含小写字母的正则表达式
    private static final Pattern LOWERCASE_PATTERN = Pattern.compile("[a-z]");
    // 包含大写字母的正则表达式
    private static final Pattern UPPERCASE_PATTERN = Pattern.compile("[A-Z]");
    // 包含特殊字符的正则表达式
    private static final Pattern SPECIAL_CHAR_PATTERN = Pattern.compile("[!@#$%^&*()_+\\-=\\[\\]{};':,.<>?]");

    /**
     * 验证密码强度
     * @param password 待验证的密码
     * @return 密码强度等级：1-弱，2-中等，3-强
     */
    public static int checkPasswordStrength(String password) {
        if (password == null) {
            return 0;
        }

        int strength = 0;

        // 检查密码长度
        if (password.length() >= MIN_LENGTH) {
            strength++;
        }

        // 检查是否包含数字
        if (DIGIT_PATTERN.matcher(password).find()) {
            strength++;
        }

        // 检查是否包含小写字母
        if (LOWERCASE_PATTERN.matcher(password).find()) {
            strength++;
        }

        // 检查是否包含大写字母
        if (UPPERCASE_PATTERN.matcher(password).find()) {
            strength++;
        }

        // 检查是否包含特殊字符
        if (SPECIAL_CHAR_PATTERN.matcher(password).find()) {
            strength++;
        }

        // 根据强度计算等级
        if (strength < 2) {
            return 1; // 弱
        } else if (strength < 4) {
            return 2; // 中等
        } else {
            return 3; // 强
        }
    }

    /**
     * 验证密码是否符合强度要求
     * @param password 待验证的密码
     * @return 是否符合要求
     */
    public static boolean isPasswordStrongEnough(String password) {
        if (password == null) {
            return false;
        }
        
        // 检查所有要求是否都满足
        return password.length() >= MIN_LENGTH &&
               DIGIT_PATTERN.matcher(password).find() &&
               LOWERCASE_PATTERN.matcher(password).find() &&
               UPPERCASE_PATTERN.matcher(password).find() &&
               SPECIAL_CHAR_PATTERN.matcher(password).find();
    }

    /**
     * 获取密码强度提示信息
     * @param password 待验证的密码
     * @return 强度提示信息
     */
    public static String getPasswordStrengthMessage(String password) {
        if (password == null) {
            return "密码不能为空";
        }

        StringBuilder message = new StringBuilder();

        if (password.length() < MIN_LENGTH) {
            message.append("密码长度至少8位；");
        }

        if (!DIGIT_PATTERN.matcher(password).find()) {
            message.append("密码必须包含数字；");
        }

        if (!LOWERCASE_PATTERN.matcher(password).find()) {
            message.append("密码必须包含小写字母；");
        }

        if (!UPPERCASE_PATTERN.matcher(password).find()) {
            message.append("密码必须包含大写字母；");
        }

        if (!SPECIAL_CHAR_PATTERN.matcher(password).find()) {
            message.append("密码必须包含特殊字符；");
        }

        if (message.length() == 0) {
            message.append("密码强度：强");
        }

        return message.toString();
    }
}
