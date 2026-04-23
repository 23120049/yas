package com.yas.media.utils;

import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StringUtilsTest {

    private static boolean hasText(String input) {
        try {
            Class<?> clazz = Class.forName("com.yas.media.utils.StringUtils");
            Method method = clazz.getDeclaredMethod("hasText", String.class);
            Object result = method.invoke(null, input);
            return (boolean) result;
        } catch (ReflectiveOperationException ex) {
            throw new AssertionError("Failed to invoke StringUtils.hasText via reflection", ex);
        }
    }

    @Test
    void hasText_whenNull_thenFalse() {
        assertFalse(hasText(null));
    }

    @Test
    void hasText_whenEmpty_thenFalse() {
        assertFalse(hasText(""));
    }

    @Test
    void hasText_whenWhitespaceOnly_thenFalse() {
        assertFalse(hasText("   "));
    }

    @Test
    void hasText_whenHasText_thenTrue() {
        assertTrue(hasText("Ngoc"));
        assertTrue(hasText(" a "));
    }
}