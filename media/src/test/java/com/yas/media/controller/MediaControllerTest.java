package com.yas.media.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.yas.media.model.Media;
import com.yas.media.model.dto.MediaDto;
import com.yas.media.service.MediaService;
import com.yas.media.viewmodel.MediaVm;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@ExtendWith(MockitoExtension.class)
class MediaControllerTest {

    @Mock
    private MediaService mediaService;

    @InjectMocks
    private MediaController mediaController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        // Dummy Validator: Luôn báo hợp lệ để vượt qua chốt chặn @Valid của MediaPostVm
        Validator mockValidator = new Validator() {
            @Override
            public boolean supports(Class<?> clazz) { return true; }
            @Override
            public void validate(Object target, Errors errors) {}
        };

        this.mockMvc = MockMvcBuilders.standaloneSetup(mediaController)
            .setValidator(mockValidator)
            .build();
    }

    // 1. Test POST /medias
    @Test
    void createMedia_whenValid_thenReturn200() throws Exception {
        MockMultipartFile file = new MockMultipartFile("multipartFile", "test.png", "image/png", "dummy".getBytes());
        Media savedMedia = new Media();
        savedMedia.setId(10L);
        savedMedia.setCaption("Test Caption");
        savedMedia.setFileName("test.png");
        savedMedia.setMediaType("image/png");

        when(mediaService.saveMedia(any())).thenReturn(savedMedia);

        mockMvc.perform(multipart("/medias")
                .file(file)
                .contentType(MediaType.MULTIPART_FORM_DATA))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(10L))
            .andExpect(jsonPath("$.caption").value("Test Caption"));

        verify(mediaService).saveMedia(any());
    }

    // 2. Test DELETE /medias/{id}
    @Test
    void deleteMedia_whenCalled_thenReturn204() throws Exception {
        doNothing().when(mediaService).removeMedia(1L);

        mockMvc.perform(delete("/medias/1"))
            .andExpect(status().isNoContent());

        verify(mediaService).removeMedia(1L);
    }

    // 3. Test GET /medias/{id} (Thành công)
    @Test
    void getMedia_whenExists_thenReturn200() throws Exception {
        MediaVm vm = new MediaVm(1L, "Caption", "file.png", "image/png", "url");
        when(mediaService.getMediaById(1L)).thenReturn(vm);

        mockMvc.perform(get("/medias/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1L))
            .andExpect(jsonPath("$.caption").value("Caption"));
    }

    // 3.1 Test GET /medias/{id} (Không tìm thấy -> 404)
    @Test
    void getMedia_whenNull_thenReturn404() throws Exception {
        when(mediaService.getMediaById(1L)).thenReturn(null);

        mockMvc.perform(get("/medias/1"))
            .andExpect(status().isNotFound());
    }

    // 4. Test GET /medias?ids=... (Thành công)
    @Test
    void getMediasByIds_whenExists_thenReturn200() throws Exception {
        MediaVm vm = new MediaVm(1L, "Cap", "file.png", "image/png", "url");
        when(mediaService.getMediaByIds(anyList())).thenReturn(List.of(vm));

        mockMvc.perform(get("/medias").param("ids", "1,2"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(1L));
    }

    // 4.1 Test GET /medias?ids=... (List rỗng -> 404)
    @Test
    void getMediasByIds_whenEmpty_thenReturn404() throws Exception {
        when(mediaService.getMediaByIds(anyList())).thenReturn(List.of());

        mockMvc.perform(get("/medias").param("ids", "1,2"))
            .andExpect(status().isNotFound());
    }

    // 5. Test GET /medias/{id}/file/{fileName}
    @Test
    void getFile_whenCalled_thenReturnInputStream() throws Exception {
        MediaDto mockDto = mock(MediaDto.class);
        InputStream is = new ByteArrayInputStream("dummy content".getBytes());
        
        when(mockDto.getMediaType()).thenReturn(MediaType.IMAGE_PNG);
        when(mockDto.getContent()).thenReturn(is);
        when(mediaService.getFile(anyLong(), anyString())).thenReturn(mockDto);

        mockMvc.perform(get("/medias/1/file/test.png"))
            .andExpect(status().isOk())
            .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"test.png\""))
            .andExpect(content().contentType(MediaType.IMAGE_PNG))
            .andExpect(content().bytes("dummy content".getBytes()));
    }
}
