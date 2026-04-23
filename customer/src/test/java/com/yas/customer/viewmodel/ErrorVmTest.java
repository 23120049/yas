package com.yas.customer.viewmodel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;
import org.junit.jupiter.api.Test;

class ErrorVmTest {

    @Test
    void testSecondaryConstructor_initializesEmptyFieldErrors() {
        ErrorVm vm = new ErrorVm("400", "Bad Request", "Invalid input");

        assertEquals("400", vm.statusCode());
        assertEquals("Bad Request", vm.title());
        assertEquals("Invalid input", vm.detail());
        assertNotNull(vm.fieldErrors());
        assertEquals(0, vm.fieldErrors().size());
    }

    @Test
    void testCanonicalConstructor_keepsProvidedFieldErrors() {
        List<String> errors = List.of("email must not be blank");
        ErrorVm vm = new ErrorVm("400", "Bad Request", "Invalid input", errors);

        assertEquals(errors, vm.fieldErrors());
    }
}
