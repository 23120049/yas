package com.yas.customer.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class MessagesUtilsTest {

    @Test
    void testGetMessageWithValidCode() {
        String result = MessagesUtils.getMessage("WRONG_EMAIL_FORMAT", "World");
        assertEquals("Wrong email format for World", result);
    }

    @Test
    void testGetMessageWithInvalidCode() {

        String result = MessagesUtils.getMessage("invalid.code");
        assertEquals("invalid.code", result);
    }

    @Test
    void testGetMessageWithEmptyCode_returnsEmptyString() {
        String result = MessagesUtils.getMessage("");
        assertEquals("", result);
    }

    @Test
    void testGetMessageWithNullCode_throwsException() {
        assertThrows(NullPointerException.class, () -> MessagesUtils.getMessage(null));
    }

    @Test
    void testGetMessageWithMissingArgs_keepsPlaceholders() {
        String result = MessagesUtils.getMessage("WRONG_EMAIL_FORMAT");
        assertEquals("Wrong email format for {}", result);
    }

    @Test
    void testGetMessageWithExtraArgs_ignoresExtraArgs() {
        String result = MessagesUtils.getMessage("USER_NOT_FOUND", "ignored");
        assertEquals("User not found", result);
    }
}
