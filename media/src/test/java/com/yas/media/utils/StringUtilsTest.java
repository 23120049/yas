package com.yas.media.utils;

import org.junit.jupiter.api.Test;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.*;

class StringUtilsTest {

    @Test
    void hasText_whenNull_thenFalse() {
        assertFalse(StringUtils.hasText(null));
    }

    @Test
    void hasText_whenEmpty_thenFalse() {
        assertFalse(StringUtils.hasText(""));
    }

    @Test
    void hasText_whenWhitespaceOnly_thenFalse() {
        assertFalse(StringUtils.hasText("   "));
    }

    @Test
    void hasText_whenHasText_thenTrue() {
        assertTrue(StringUtils.hasText("Ngoc"));
        assertTrue(StringUtils.hasText(" a "));
    }

    @Test
    void testPrivateConstructor() throws Exception {
        Constructor<StringUtils> constructor = StringUtils.class.getDeclaredConstructor();
        assertTrue(Modifier.isPrivate(constructor.getModifiers()));
        constructor.setAccessible(true);
        try {
            constructor.newInstance();
        } catch (InvocationTargetException e) {
            assertEquals(UnsupportedOperationException.class, e.getCause().getClass());
        }
    }
}
