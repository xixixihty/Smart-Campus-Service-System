package com.hxq.smart_campus.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PasswordStrengthUtilsTest {

    @Test
    public void testWeakPassword() {
        // 测试弱密码：长度足够，但缺少大写字母和特殊字符
        String weakPassword = "12xixixi";
        assertFalse(PasswordStrengthUtils.isPasswordStrongEnough(weakPassword));
        System.out.println("弱密码测试结果: " + PasswordStrengthUtils.getPasswordStrengthMessage(weakPassword));
    }

    @Test
    public void testStrongPassword() {
        // 测试强密码：满足所有条件
        String strongPassword = "Test123!";
        assertTrue(PasswordStrengthUtils.isPasswordStrongEnough(strongPassword));
        System.out.println("强密码测试结果: " + PasswordStrengthUtils.getPasswordStrengthMessage(strongPassword));
    }

    @Test
    public void testPasswordWithoutUpperCase() {
        // 测试缺少大写字母的密码
        String password = "test123!";
        assertFalse(PasswordStrengthUtils.isPasswordStrongEnough(password));
        System.out.println("缺少大写字母的密码测试结果: " + PasswordStrengthUtils.getPasswordStrengthMessage(password));
    }

    @Test
    public void testPasswordWithoutSpecialChar() {
        // 测试缺少特殊字符的密码
        String password = "Test123";
        assertFalse(PasswordStrengthUtils.isPasswordStrongEnough(password));
        System.out.println("缺少特殊字符的密码测试结果: " + PasswordStrengthUtils.getPasswordStrengthMessage(password));
    }

    @Test
    public void testPasswordWithoutDigit() {
        // 测试缺少数字的密码
        String password = "Test!@#";
        assertFalse(PasswordStrengthUtils.isPasswordStrongEnough(password));
        System.out.println("缺少数字的密码测试结果: " + PasswordStrengthUtils.getPasswordStrengthMessage(password));
    }

    @Test
    public void testPasswordWithoutLowerCase() {
        // 测试缺少小写字母的密码
        String password = "TEST123!";
        assertFalse(PasswordStrengthUtils.isPasswordStrongEnough(password));
        System.out.println("缺少小写字母的密码测试结果: " + PasswordStrengthUtils.getPasswordStrengthMessage(password));
    }

    @Test
    public void testPasswordTooShort() {
        // 测试长度不足的密码
        String password = "Test1!";
        assertFalse(PasswordStrengthUtils.isPasswordStrongEnough(password));
        System.out.println("长度不足的密码测试结果: " + PasswordStrengthUtils.getPasswordStrengthMessage(password));
    }
}
