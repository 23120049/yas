package com.yas.media.viewmodel;

import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NoFileMediaVmAndErrorVmTest {

    @Test
    void noFileMediaVm_constructorAndAccessors() {
        Long id = 1L;
        String caption = "caption";
        String fileName = "file.png";
        String mediaType = "image/png";

        NoFileMediaVm vm = new NoFileMediaVm(id, caption, fileName, mediaType);

        assertEquals(id, vm.id());
        assertEquals(caption, vm.caption());
        assertEquals(fileName, vm.fileName());
        assertEquals(mediaType, vm.mediaType());
    }

    @Test
    void errorVm_fullConstructorAndAccessors() {
        List<String> fieldErrors = List.of("caption must not be blank", "multipartFile must not be null");

        ErrorVm vm = new ErrorVm("400 BAD_REQUEST", "Bad Request", "Request information is not valid", fieldErrors);

        assertEquals("400 BAD_REQUEST", vm.statusCode());
        assertEquals("Bad Request", vm.title());
        assertEquals("Request information is not valid", vm.detail());
        assertEquals(fieldErrors, vm.fieldErrors());
    }

    @Test
    void errorVm_3ArgConstructor_createsEmptyFieldErrorsList() {
        ErrorVm vm = new ErrorVm("404 NOT_FOUND", "Not Found", "Media not found");

        assertEquals("404 NOT_FOUND", vm.statusCode());
        assertEquals("Not Found", vm.title());
        assertEquals("Media not found", vm.detail());
        assertNotNull(vm.fieldErrors());
        assertTrue(vm.fieldErrors().isEmpty());
    }
}
