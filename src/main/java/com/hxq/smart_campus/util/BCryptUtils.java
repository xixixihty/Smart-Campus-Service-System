package com.hxq.smart_campus.util;

import cn.hutool.crypto.digest.BCrypt;
import lombok.extern.slf4j.Slf4j;
/**
 * BCrypt哈希工具类
 * @author hxq
 * @description BCrypt哈希工具类，用于加密密码和验证密码
 */
@Slf4j
public class BCryptUtils {

    private static final int BCRYPT_HASH_LENGTH = 60;
    private static final int BCRYPT_MIN_LENGTH = 59;

    /**
     * 加密密码
     * @param password 明文密码
     * @return 加密后的密码
     */
    public static String encryptPassword(String password) {
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("密码不能为空");
        }
        String encrypted = BCrypt.hashpw(password, BCrypt.gensalt(12));
        log.debug("密码加密成功，哈希长度：{}", encrypted.length());
        return encrypted;
    }

    /**
     * 验证密码
     * @param password 明文密码
     * @param hashedPassword 加密后的密码
     * @return 验证结果
     */
    public static boolean checkPassword(String password, String hashedPassword) {
        if (password == null || password.isEmpty()) {
            log.warn("验证密码失败：明文密码为空");
            throw new IllegalArgumentException("密码不能为空");
        }
        if (hashedPassword == null || hashedPassword.isEmpty()) {
            log.warn("验证密码失败：哈希密码为空");
            throw new IllegalArgumentException("哈希密码不能为空");
        }

        // 检查哈希长度是否足够（防止截断的哈希）
        if (hashedPassword.length() < BCRYPT_MIN_LENGTH) {
            log.warn("密码哈希长度不足！当前长度：{}，预期长度：{}，哈希值：{}",
                    hashedPassword.length(), BCRYPT_HASH_LENGTH, hashedPassword);
            // 允许59字符的哈希（可能是被截断的）
            if (hashedPassword.length() < 59) {
                throw new IllegalArgumentException("密码哈希格式错误，长度不足");
            }
        }

        try {
            boolean result = BCrypt.checkpw(password, hashedPassword);
            if (!result) {
                log.warn("密码验证失败，密码长度：{}，哈希长度：{}", password.length(), hashedPassword.length());
                log.warn("哈希前缀：{}", hashedPassword.substring(0, 7));
            }
            return result;
        } catch (Exception e) {
            log.error("密码验证异常：{}", e.getMessage());
            throw new IllegalArgumentException("密码验证失败：" + e.getMessage());
        }
    }

    /**
     * 检查哈希是否为有效的BCrypt格式
     * @param hashedPassword 哈希密码
     * @return 是否有效
     */
    public static boolean isValidBCryptHash(String hashedPassword) {
        if (hashedPassword == null || hashedPassword.length() < BCRYPT_MIN_LENGTH) {
            return false;
        }
        return hashedPassword.matches("^\\$2[ay]\\$\\d{2}\\$.{53}$");
    }
}
