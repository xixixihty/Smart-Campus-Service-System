package com.hxq.smart_campus.util;

import cn.hutool.crypto.digest.BCrypt;
/**
 * BCrypt哈希生成工具类
 * @author hxq
 * @description BCrypt哈希生成工具类，用于生成BCrypt哈希值和验证哈希值
 */
public class BCryptGenerator {
    public static void main(String[] args) {
        // 生成正确的哈希值
        String password = "Password123!";
        String hash10 = BCrypt.hashpw(password, BCrypt.gensalt(10));
        String hash12 = BCrypt.hashpw(password, BCrypt.gensalt(12));
        
        System.out.println("密码: " + password);
        System.out.println("10轮哈希: " + hash10);
        System.out.println("12轮哈希: " + hash12);
        System.out.println("10轮哈希长度: " + hash10.length());
        System.out.println("12轮哈希长度: " + hash12.length());
        
        // 验证
        boolean check10 = BCrypt.checkpw(password, hash10);
        boolean check12 = BCrypt.checkpw(password, hash12);
        System.out.println("10轮验证: " + check10);
        System.out.println("12轮验证: " + check12);
    }
}