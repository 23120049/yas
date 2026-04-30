package com.yas.media;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.yas.commonlibrary.exception.NotFoundException;
import com.yas.commonlibrary.mapper.BaseMapper;
import com.yas.media.config.YasConfig;
import com.yas.media.mapper.MediaVmMapper;
import com.yas.media.model.Media;
import com.yas.media.model.dto.MediaDto;
import com.yas.media.repository.FileSystemRepository;
import com.yas.media.repository.MediaRepository;
import com.yas.media.service.MediaServiceImpl;
import com.yas.media.viewmodel.MediaPostVm;
import com.yas.media.viewmodel.MediaVm;
import com.yas.media.viewmodel.NoFileMediaVm;
import java.util.List;
import java.io.IOException;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

class MediaServiceUnitTest {

    @Mock
    private MediaVmMapper mediaVmMapper;

    @Mock
    private MediaRepository mediaRepository;

    @Mock
    private FileSystemRepository fileSystemRepository;

    @Mock
    private YasConfig yasConfig;

    @InjectMocks
    private MediaServiceImpl mediaService;

    private Media media;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        media = new Media();
        media.setId(1L);
        media.setCaption("test");
        media.setFileName("file");
        media.setMediaType("image/jpeg");
    }

    @Test
    void getMedia_whenValidId_thenReturnData() {
        NoFileMediaVm noFileMediaVm = new NoFileMediaVm(1L, "Test", "fileName", "image/png");
        when(mediaRepository.findByIdWithoutFileInReturn(1L)).thenReturn(noFileMediaVm);
        when(yasConfig.publicUrl()).thenReturn("/media");

        MediaVm mediaVm = mediaService.getMediaById(1L);
        assertNotNull(mediaVm);
        assertEquals("Test", mediaVm.getCaption());
        assertEquals("fileName", mediaVm.getFileName());
        assertEquals("image/png", mediaVm.getMediaType());
        assertEquals("/media/medias/1/file/fileName", mediaVm.getUrl());
    }

    @Test
    void getMedia_whenMediaNotFound_thenReturnNull() {
        when(mediaRepository.findByIdWithoutFileInReturn(1L)).thenReturn(null);
        assertNull(mediaService.getMediaById(1L));
    }

    @Test
    void removeMedia_whenMediaNotFound_thenThrowsNotFoundException() {
        when(mediaRepository.findByIdWithoutFileInReturn(1L)).thenReturn(null);

        NotFoundException exception = assertThrows(NotFoundException.class, () -> mediaService.removeMedia(1L));
        assertEquals(String.format("Media %s is not found", 1L), exception.getMessage());
    }

    @Test
    void removeMedia_whenValidId_thenRemoveSuccess() {
        NoFileMediaVm noFileMediaVm = new NoFileMediaVm(1L, "Test", "fileName", "image/png");
        when(mediaRepository.findByIdWithoutFileInReturn(1L)).thenReturn(noFileMediaVm);

        mediaService.removeMedia(1L);

        verify(mediaRepository, times(1)).deleteById(1L);
    }

    @Test
    void saveMedia_whenTypePNG_thenSaveSuccess() throws IOException {
        byte[] pngFileContent = new byte[] {};
        MultipartFile multipartFile = new MockMultipartFile("file", "example.png", "image/png", pngFileContent);
        MediaPostVm mediaPostVm = new MediaPostVm("media", multipartFile, "fileName");

        when(fileSystemRepository.persistFile(any(), any())).thenReturn("C:/tmp/file");
        when(mediaRepository.save(any(Media.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Media mediaSave = mediaService.saveMedia(mediaPostVm);
        assertNotNull(mediaSave);
        assertEquals("media", mediaSave.getCaption());
        assertEquals("fileName", mediaSave.getFileName());
    }

    @Test
    void saveMedia_whenTypeJPEG_thenSaveSuccess() throws IOException {
        byte[] pngFileContent = new byte[] {};
        MultipartFile multipartFile = new MockMultipartFile("file", "example.jpeg", "image/jpeg", pngFileContent);
        MediaPostVm mediaPostVm = new MediaPostVm("media", multipartFile, "fileName");

        when(fileSystemRepository.persistFile(any(), any())).thenReturn("C:/tmp/file");
        when(mediaRepository.save(any(Media.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Media mediaSave = mediaService.saveMedia(mediaPostVm);
        assertNotNull(mediaSave);
        assertEquals("media", mediaSave.getCaption());
        assertEquals("fileName", mediaSave.getFileName());
    }

    @Test
    void saveMedia_whenTypeGIF_thenSaveSuccess() throws IOException {
        byte[] gifFileContent = new byte[] {};
        MultipartFile multipartFile = new MockMultipartFile("file", "example.gif", "image/gif", gifFileContent);
        MediaPostVm mediaPostVm = new MediaPostVm("media", multipartFile, "fileName");

        when(fileSystemRepository.persistFile(any(), any())).thenReturn("C:/tmp/file");
        when(mediaRepository.save(any(Media.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Media mediaSave = mediaService.saveMedia(mediaPostVm);
        assertNotNull(mediaSave);
        assertEquals("media", mediaSave.getCaption());
        assertEquals("fileName", mediaSave.getFileName());
    }

    @Test
    void saveMedia_whenFileNameIsNull_thenOk() throws IOException {
        byte[] pngFileContent = new byte[] {};
        MultipartFile multipartFile = new MockMultipartFile("file", "example.png", "image/png", pngFileContent);
        MediaPostVm mediaPostVm = new MediaPostVm("media", multipartFile, null);

        when(fileSystemRepository.persistFile(any(), any())).thenReturn("C:/tmp/file");
        when(mediaRepository.save(any(Media.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Media mediaSave = mediaService.saveMedia(mediaPostVm);
        assertNotNull(mediaSave);
        assertEquals("media", mediaSave.getCaption());
        assertEquals("example.png", mediaSave.getFileName());
    }

    @Test
    void saveMedia_whenFileNameIsEmpty_thenOk() throws IOException {
        byte[] pngFileContent = new byte[] {};
        MultipartFile multipartFile = new MockMultipartFile("file", "example.png", "image/png", pngFileContent);
        MediaPostVm mediaPostVm = new MediaPostVm("media", multipartFile, "");

        when(fileSystemRepository.persistFile(any(), any())).thenReturn("C:/tmp/file");
        when(mediaRepository.save(any(Media.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Media mediaSave = mediaService.saveMedia(mediaPostVm);
        assertNotNull(mediaSave);
        assertEquals("media", mediaSave.getCaption());
        assertEquals("example.png", mediaSave.getFileName());
    }

    @Test
    void saveMedia_whenFileNameIsBlank_thenOk() throws IOException {
        byte[] pngFileContent = new byte[] {};
        MultipartFile multipartFile = new MockMultipartFile("file", "example.png", "image/png", pngFileContent);
        MediaPostVm mediaPostVm = new MediaPostVm("media", multipartFile, "   ");

        when(fileSystemRepository.persistFile(any(), any())).thenReturn("C:/tmp/file");
        when(mediaRepository.save(any(Media.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Media mediaSave = mediaService.saveMedia(mediaPostVm);
        assertNotNull(mediaSave);
        assertEquals("media", mediaSave.getCaption());
        assertEquals("example.png", mediaSave.getFileName());
    }

    @Test
    void getFile_whenMediaNotFound_thenReturnEmptyMediaDto() {
        when(mediaRepository.findById(1L)).thenReturn(Optional.empty());

        MediaDto result = mediaService.getFile(1L, "fileName");
        assertNotNull(result);
        assertNull(result.getContent());
    }

    @Test
    void getFile_whenMediaNameNotMatch_thenReturnEmptyMediaDto() {
        when(mediaRepository.findById(1L)).thenReturn(Optional.ofNullable(media));

        MediaDto result = mediaService.getFile(1L, "wrongName");
        assertNotNull(result);
        assertNull(result.getContent());
    }

    @Test
    void getMediaByIds() {
        var ip15 = getMedia(1L, "Iphone 15");
        var macbook = getMedia(2L, "Macbook");
        var existingMedias = List.of(ip15, macbook);
        
        when(mediaRepository.findAllById(List.of(1L, 2L))).thenReturn(existingMedias);
        when(yasConfig.publicUrl()).thenReturn("https://media");
        
        // FIX: Ép kiểu tường minh về BaseMapper để javac không bị lú
        when(((BaseMapper<Media, MediaVm>) mediaVmMapper).toVm(ip15)).thenReturn(new MediaVm(1L, "Iphone 15", "Iphone 15", "image", ""));
        when(((BaseMapper<Media, MediaVm>) mediaVmMapper).toVm(macbook)).thenReturn(new MediaVm(2L, "Macbook", "Macbook", "image", ""));

        var medias = mediaService.getMediaByIds(List.of(1L, 2L));

        assertFalse(medias.isEmpty());
        assertEquals(2, medias.size());
    }

    private static @NotNull Media getMedia(Long id, String name) {
        var media = new Media();
        media.setId(id);
        media.setFileName(name);
        return media;
    }
}
