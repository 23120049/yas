package com.yas.payment.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class MessagesUtilsTest {

    @Test
    void getMessage_whenKeyExists_formatsMessageWithArgs() {
        String message = MessagesUtils.getMessage(Constants.ErrorCode.PAYMENT_PROVIDER_NOT_FOUND, "paypal");

        assertThat(message).isEqualTo("Payment provider paypal is not found");
    }

    @Test
    void getMessage_whenKeyMissing_returnsErrorCodeAsMessage() {
        String message = MessagesUtils.getMessage("SOME_UNKNOWN_CODE");

        assertThat(message).isEqualTo("SOME_UNKNOWN_CODE");
    }
}
