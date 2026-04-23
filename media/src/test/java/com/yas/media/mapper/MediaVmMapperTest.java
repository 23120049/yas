package com.yas.media.mapper;

import com.yas.media.viewmodel.MediaVm;
import com.yas.media.viewmodel.NoFileMediaVm;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MediaVmMapperTest {
    @Test
    void testViewModels() {
        MediaVm vm = new MediaVm(1L, "Cap", "file", "img", "url");
        assertEquals(1L, vm.getId());
        
        NoFileMediaVm noFile = new NoFileMediaVm(2L, "Cap2", "file2", "img2");
        assertEquals(2L, noFile.id()); // NoFileMediaVm thường là Record nên dùng .id()
    }
}
