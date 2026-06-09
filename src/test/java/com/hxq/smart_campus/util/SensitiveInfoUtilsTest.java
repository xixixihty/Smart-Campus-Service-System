package com.hxq.smart_campus.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SensitiveInfoUtilsTest {

    @Test
    public void testMaskPassword() {
        String password = "Password123!";
        String maskedPassword = SensitiveInfoUtils.maskPassword(password);
        assertEquals("******", maskedPassword);

        String emptyPassword = "";
        String maskedEmptyPassword = SensitiveInfoUtils.maskPassword(emptyPassword);
        assertEquals("", maskedEmptyPassword);

        String nullPassword = null;
        String maskedNullPassword = SensitiveInfoUtils.maskPassword(nullPassword);
        assertEquals("", maskedNullPassword);
    }

    @Test
    public void testMaskPhone() {
        String phone = "13812345678";
        String maskedPhone = SensitiveInfoUtils.maskPhone(phone);
        assertEquals("138****5678", maskedPhone);

        String shortPhone = "12345";
        String maskedShortPhone = SensitiveInfoUtils.maskPhone(shortPhone);
        assertEquals("12345", maskedShortPhone);

        String nullPhone = null;
        String maskedNullPhone = SensitiveInfoUtils.maskPhone(nullPhone);
        assertNull(maskedNullPhone);
    }

    @Test
    public void testMaskIdCard() {
        String idCard = "110101199001011234";
        String maskedIdCard = SensitiveInfoUtils.maskIdCard(idCard);
        assertEquals("110***********1234", maskedIdCard);

        String shortIdCard = "11010119900101";
        String maskedShortIdCard = SensitiveInfoUtils.maskIdCard(shortIdCard);
        assertEquals("11010119900101", maskedShortIdCard);

        String nullIdCard = null;
        String maskedNullIdCard = SensitiveInfoUtils.maskIdCard(nullIdCard);
        assertNull(maskedNullIdCard);
    }
}
