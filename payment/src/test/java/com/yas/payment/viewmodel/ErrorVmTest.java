package com.yas.payment.viewmodel;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class ErrorVmTest {

    @Test
    void constructor_withFieldErrors_populatesAllFields() {
        List<String> fieldErrors = new ArrayList<>(List.of("field is required"));

        ErrorVm vm = new ErrorVm("400", "Bad Request", "Invalid", fieldErrors);

        assertThat(vm.statusCode()).isEqualTo("400");
        assertThat(vm.title()).isEqualTo("Bad Request");
        assertThat(vm.detail()).isEqualTo("Invalid");
        assertThat(vm.fieldErrors()).containsExactly("field is required");
    }

    @Test
    void constructor_withoutFieldErrors_initializesEmptyList() {
        ErrorVm vm = new ErrorVm("500", "Error", "Oops");

        assertThat(vm.fieldErrors()).isNotNull();
        assertThat(vm.fieldErrors()).isEmpty();
    }
}
