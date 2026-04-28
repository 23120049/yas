package com.yas.payment.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Constructor;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class ConstantsTest {

    @Test
    void constructors_whenInvoked_coverUtilityConstructors() throws Exception {
        Constants constants = new Constants();
        assertThat(constants).isNotNull();

        Constants.Message message = constants.new Message();
        assertThat(message).isNotNull();

        Constructor<Constants.ErrorCode> ctor = Constants.ErrorCode.class.getDeclaredConstructor(Constants.class);
        ctor.setAccessible(true);
        Constants.ErrorCode errorCode = ctor.newInstance(constants);
        assertThat(errorCode).isNotNull();
    }
}
