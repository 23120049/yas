package com.yas.media.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
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
import com.yas.media.viewmodel.MediaPostVm;
import com.yas.media.viewmodel.MediaVm;
import com.yas.media.viewmodel.NoFileMediaVm;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class MediaServiceImplTest {

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

    @Test
    void saveMedia_whenFileNameOverrideHasText_thenTrimAndPersistWithOverride() throws IOException {
        byte[] content = new byte[] { 1, 2, 3 };
        MultipartFile multipartFile = new MockMultipartFile("multipartFile", "original.png", "image/png", content);
        MediaPostVm vm = new MediaPostVm("Caption", multipartFile, "  override.png  ");

        when(fileSystemRepository.persistFile(eq("override.png"), eq(content))).thenReturn("C:/tmp/override.png");
        when(mediaRepository.save(any(Media.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Media saved = mediaService.saveMedia(vm);

        assertNotNull(saved);
        assertEquals("Caption", saved.getCaption());
        assertEquals("image/png", saved.getMediaType());
        assertEquals("override.png", saved.getFileName());
        assertEquals("C:/tmp/override.png", saved.getFilePath());
        verify(fileSystemRepository, times(1)).persistFile(eq("override.png"), eq(content));
    }

    @Test
    void saveMedia_whenFileNameOverrideBlank_thenUsesOriginalFilename() throws IOException {
        byte[] content = "x".getBytes();
        MultipartFile multipartFile = new MockMultipartFile("multipartFile", "original-name.jpg", "image/jpeg", content);
        MediaPostVm vm = new MediaPostVm("Caption", multipartFile, "   ");

        when(fileSystemRepository.persistFile(eq("original-name.jpg"), eq(content))).thenReturn("C:/tmp/original-name.jpg");
        when(mediaRepository.save(any(Media.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Media saved = mediaService.saveMedia(vm);

        assertEquals("original-name.jpg", saved.getFileName());
        assertEquals("C:/tmp/original-name.jpg", saved.getFilePath());
        verify(fileSystemRepository, times(1)).persistFile(eq("original-name.jpg"), eq(content));
    }

    @Test
    void saveMedia_whenPersistFileThrowsIOException_thenPropagates() throws IOException {
        byte[] content = "x".getBytes();
        MultipartFile multipartFile = new MockMultipartFile("multipartFile", "original.png", "image/png", content);
        MediaPostVm vm = new MediaPostVm("Caption", multipartFile, null);

        when(fileSystemRepository.persistFile(eq("original.png"), eq(content))).thenThrow(new IOException("boom"));

        assertThrows(IOException.class, () -> mediaService.saveMedia(vm));
        verify(mediaRepository, never()).save(any(Media.class));
    }

    @Test
    void getMediaById_whenMediaExists_thenReturnMediaVmWithUrl() {
        Long mediaId = 1L;
        NoFileMediaVm noFileMediaVm = new NoFileMediaVm(mediaId, "Caption", "image.png", "image/png");
        when(mediaRepository.findByIdWithoutFileInReturn(mediaId)).thenReturn(noFileMediaVm);
        when(yasConfig.publicUrl()).thenReturn("https://cdn.example.com");

        MediaVm result = mediaService.getMediaById(mediaId);

        assertNotNull(result);
        assertEquals(mediaId, result.getId());
        assertEquals("https://cdn.example.com/medias/1/file/image.png", result.getUrl());
    }

    @Test
    void getMediaById_whenMediaDoesNotExist_thenReturnNull() {
        Long mediaId = 999L;
        when(mediaRepository.findByIdWithoutFileInReturn(mediaId)).thenReturn(null);

        assertNull(mediaService.getMediaById(mediaId));
    }

    @Test
    void getMediaByIds_whenIdsExist_thenReturnMappedListWithUrl() {
        List<Long> ids = List.of(1L, 2L);
        Media media1 = buildMedia(1L, "image-1.png", "image/png", "caption-1");
        Media media2 = buildMedia(2L, "image-2.jpg", "image/jpeg", "caption-2");
        when(mediaRepository.findAllById(ids)).thenReturn(List.of(media1, media2));
        
        when(((BaseMapper<Media, MediaVm>) mediaVmMapper).toVm(media1)).thenReturn(buildMediaVm(1L, "caption-1", "image-1.png", "image/png"));
        when(((BaseMapper<Media, MediaVm>) mediaVmMapper).toVm(media2)).thenReturn(buildMediaVm(2L, "caption-2", "image-2.jpg", "image/jpeg"));
        when(yasConfig.publicUrl()).thenReturn("https://cdn.example.com");

        List<MediaVm> result = mediaService.getMediaByIds(ids);

        assertEquals(2, result.size());
    }

    @Test
    void getMediaByIds_whenIdsIsEmpty_thenReturnEmptyList() {
        List<Long> ids = List.of();
        when(mediaRepository.findAllById(ids)).thenReturn(List.of());

        List<MediaVm> result = mediaService.getMediaByIds(ids);

        assertTrue(result.isEmpty());
    }

    @Test
    void getMediaByIds_whenSomeIdsDoNotExist_thenReturnOnlyExistingMappedItems() {
        List<Long> ids = List.of(1L, 2L, 3L);
        Media media1 = buildMedia(1L, "image-1.png", "image/png", "caption-1");
        Media media3 = buildMedia(3L, "image-3.png", "image/png", "caption-3");
        when(mediaRepository.findAllById(ids)).thenReturn(List.of(media1, media3));
        
        // FIX: Ép kiểu tường minh về BaseMapper để javac không bị lú
        when(((BaseMapper<Media, MediaVm>) mediaVmMapper).toVm(media1)).thenReturn(buildMediaVm(1L, "caption-1", "image-1.png", "image/png"));
        when(((BaseMapper<Media, MediaVm>) mediaVmMapper).toVm(media3)).thenReturn(buildMediaVm(3L, "caption-3", "image-3.png", "image/png"));
        when(yasConfig.publicUrl()).thenReturn("https://cdn.example.com");

        List<MediaVm> result = mediaService.getMediaByIds(ids);

        assertEquals(2, result.size());
    }

    @Test
    void getFile_whenMediaExistsAndFileNameMatches_thenReturnContentAndMediaType() {
        Long mediaId = 10L;
        String fileName = "banner.png";
        String filePath = "C:/tmp/banner.png";
        Media media = buildMedia(mediaId, fileName, "image/png", "banner");
        media.setFilePath(filePath);
        InputStream content = new ByteArrayInputStream(new byte[] { 1, 2, 3 });

        when(mediaRepository.findById(mediaId)).thenReturn(Optional.of(media));
        when(fileSystemRepository.getFile(filePath)).thenReturn(content);

        MediaDto result = mediaService.getFile(mediaId, fileName);

        assertNotNull(result);
        assertEquals(MediaType.IMAGE_PNG, result.getMediaType());
        assertEquals(content, result.getContent());
    }

    @Test
    void getFile_whenFileNameDoesNotMatch_thenReturnEmptyDto() {
        Long mediaId = 11L;
        Media media = buildMedia(mediaId, "actual.png", "image/png", "actual");
        when(mediaRepository.findById(mediaId)).thenReturn(Optional.of(media));

        MediaDto result = mediaService.getFile(mediaId, "wrong.png");
        assertNull(result.getContent());
    }

    @Test
    void removeMedia_whenMediaExists_thenDeleteFromDatabase() {
        Long mediaId = 12L;
        NoFileMediaVm noFileMediaVm = new NoFileMediaVm(mediaId, "to-delete", "delete-me.png", "image/png");
        when(mediaRepository.findByIdWithoutFileInReturn(mediaId)).thenReturn(noFileMediaVm);

        mediaService.removeMedia(mediaId);

        verify(mediaRepository, times(1)).deleteById(mediaId);
    }

    @Test
    void removeMedia_whenMediaDoesNotExist_thenThrowNotFoundExceptionAndSkipDeletion() {
        Long mediaId = 13L;
        when(mediaRepository.findByIdWithoutFileInReturn(mediaId)).thenReturn(null);

        assertThrows(NotFoundException.class, () -> mediaService.removeMedia(mediaId));
        verify(mediaRepository, never()).deleteById(any(Long.class));
    }

    private Media buildMedia(Long id, String fileName, String mediaType, String caption) {
        Media media = new Media();
        media.setId(id);
        media.setFileName(fileName);
        media.setMediaType(mediaType);
        media.setCaption(caption);
        return media;
    }

    private MediaVm buildMediaVm(Long id, String caption, String fileName, String mediaType) {
        return new MediaVm(id, caption, fileName, mediaType, null);
    }
}
