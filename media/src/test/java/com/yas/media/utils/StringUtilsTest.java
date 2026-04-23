package com.yas.media.utils;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class StringUtilsTest {
    @Test
    void testHasText_AllCases() {
        // Case đúng
        assertTrue(StringUtils.hasText("Ngoc"));
        assertTrue(StringUtils.hasText(" a "));
        // Case sai
        assertFalse(StringUtils.hasText(null));
        assertFalse(StringUtils.hasText(""));
        assertFalse(StringUtils.hasText("   "));
    }
}