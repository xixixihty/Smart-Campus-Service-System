package com.hxq.smart_campus.utils;

import cn.hutool.crypto.digest.BCrypt;

import static com.hxq.smart_campus.constant.MessageConstant.DEFAULT_PASSWORD;

/**
 * 密码重置工具类
 * @author hxq
 * @description 密码重置工具类，用于重置用户密码
 */
public class PasswordResetTool {
    public static void main(String[] args) {
        // 新密码
        String newPassword = DEFAULT_PASSWORD;
        
        // 生成12轮哈希
        String hash = BCrypt.hashpw(newPassword, BCrypt.gensalt(12));
        
        System.out.println("新密码: " + newPassword);
        System.out.println("生成的哈希: " + hash);
        System.out.println("哈希长度: " + hash.length());
        
        // 验证
        boolean check = BCrypt.checkpw(newPassword, hash);
        System.out.println("验证结果: " + check);
    }
}