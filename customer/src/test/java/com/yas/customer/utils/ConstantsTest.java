package com.yas.customer.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.lang.reflect.Field;
import org.junit.jupiter.api.Test;

class ConstantsTest {

    @Test
    void testErrorCodeConstants_areAccessible() throws Exception {
        // Execute the implicit no-args constructors to mark the classes as covered.
        // (These utility classes currently have no explicit private constructor.)
        assertNotNull(new Constants());
        assertNotNull(new Constants.ErrorCode());

        Class<?> errorCodeClass = Class.forName("com.yas.customer.utils.Constants$ErrorCode");

        for (Field field : errorCodeClass.getDeclaredFields()) {
            if (!field.getType().equals(String.class)) {
                continue;
            }
            Object value = field.get(null);
            assertNotNull(value, field.getName() + " should not be null");
        }

        assertEquals("USER_NOT_FOUND", Constants.ErrorCode.USER_NOT_FOUND);
        assertEquals("USER_ADDRESS_NOT_FOUND", Constants.ErrorCode.USER_ADDRESS_NOT_FOUND);
    }
}
