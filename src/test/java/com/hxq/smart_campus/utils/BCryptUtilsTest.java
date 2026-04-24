package com.hxq.smart_campus.utils;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class BCryptUtilsTest {

    @Test
    public void testEncryptPassword() {
        String password = "Password123!";
        String encryptedPassword = BCryptUtils.encryptPassword(password);
        assertNotNull(encryptedPassword);
        assertNotEquals(password, encryptedPassword);
        assertTrue(encryptedPassword.startsWith("$2a$"));
    }

    @Test
    public void testCheckPassword() {
        String password = "Password123!";
        String encryptedPassword = BCryptUtils.encryptPassword(password);
        assertTrue(BCryptUtils.checkPassword(password, encryptedPassword));
        assertFalse(BCryptUtils.checkPassword("WrongPassword", encryptedPassword));
    }
}
