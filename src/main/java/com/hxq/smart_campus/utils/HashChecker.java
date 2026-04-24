package com.hxq.smart_campus.utils;

import cn.hutool.crypto.digest.BCrypt;
/**
 * 哈希值检查工具类
 * @author hxq
 * @description 哈希值检查工具类，用于验证密码哈希值是否正确
 */
public class HashChecker {
    public static void main(String[] args) {
        // 数据库中的哈希值
        String dbHash = "$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi";
        
        System.out.println("数据库哈希: " + dbHash);
        System.out.println("哈希长度: " + dbHash.length());
        
        // 测试不同密码
        String[] passwords = {
            "Password123!",
            "123456",
            "password",
            "admin",
            "Password123",
            "PASSWORD123!",
            "password123!",
            "Password123!"
        };
        
        for (String password : passwords) {
            try {
                boolean result = BCrypt.checkpw(password, dbHash);
                System.out.println("密码: '" + password + "' 验证结果: " + result);
            } catch (Exception e) {
                System.out.println("密码: '" + password + "' 验证异常: " + e.getMessage());
            }
        }
        
        // 生成新的哈希进行对比
        String newHash = BCrypt.hashpw("Password123!", BCrypt.gensalt(10));
        System.out.println("\n新生成的哈希: " + newHash);
        System.out.println("新哈希长度: " + newHash.length());
    }
}