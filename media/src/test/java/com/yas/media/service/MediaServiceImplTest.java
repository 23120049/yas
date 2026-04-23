package com.yas.media.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.yas.commonlibrary.exception.NotFoundException;
import com.yas.media.config.YasConfig;
import com.yas.media.mapper.MediaVmMapper;
import com.yas.media.model.Media;
import com.yas.media.repository.FileSystemRepository;
import com.yas.media.repository.MediaRepository;
import com.yas.media.viewmodel.MediaVm;
import com.yas.media.viewmodel.NoFileMediaVm;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
    void getMediaById_whenMediaExists_thenReturnMediaVmWithUrl() {
        // Given: a media projection exists for the requested id
        Long mediaId = 1L;
        NoFileMediaVm noFileMediaVm = new NoFileMediaVm(mediaId, "Caption", "image.png", "image/png");
        when(mediaRepository.findByIdWithoutFileInReturn(mediaId)).thenReturn(noFileMediaVm);
        when(yasConfig.publicUrl()).thenReturn("https://cdn.example.com");

        // When: getMediaById is invoked
        MediaVm result = mediaService.getMediaById(mediaId);

        // Then: media data and URL are returned as expected
        assertNotNull(result);
        assertEquals(mediaId, result.getId());
        assertEquals("Caption", result.getCaption());
        assertEquals("image.png", result.getFileName());
        assertEquals("image/png", result.getMediaType());
        assertEquals("https://cdn.example.com/medias/1/file/image.png", result.getUrl());
        verify(mediaRepository, times(1)).findByIdWithoutFileInReturn(mediaId);
    }

    @Test
    void getMediaById_whenMediaDoesNotExist_thenThrowNotFoundException() {
        // Given: no media found for the requested id
        Long mediaId = 999L;
        when(mediaRepository.findByIdWithoutFileInReturn(mediaId)).thenReturn(null);

        // When + Then: service is expected to throw NotFoundException
        assertThrows(NotFoundException.class, () -> mediaService.getMediaById(mediaId));
        verify(mediaRepository, times(1)).findByIdWithoutFileInReturn(mediaId);
    }

    @Test
    void getMediaByIds_whenIdsExist_thenReturnMappedListWithUrl() {
        // Given: repository returns media entities and mapper converts them to MediaVm
        List<Long> ids = List.of(1L, 2L);
        Media media1 = buildMedia(1L, "image-1.png", "image/png", "caption-1");
        Media media2 = buildMedia(2L, "image-2.jpg", "image/jpeg", "caption-2");
        when(mediaRepository.findAllById(ids)).thenReturn(List.of(media1, media2));
        when(mediaVmMapper.toVm(media1)).thenReturn(buildMediaVm(1L, "caption-1", "image-1.png", "image/png"));
        when(mediaVmMapper.toVm(media2)).thenReturn(buildMediaVm(2L, "caption-2", "image-2.jpg", "image/jpeg"));
        when(yasConfig.publicUrl()).thenReturn("https://cdn.example.com");

        // When: getMediaByIds is invoked
        List<MediaVm> result = mediaService.getMediaByIds(ids);

        // Then: mapped list is returned with generated URLs
        assertEquals(2, result.size());
        assertEquals("https://cdn.example.com/medias/1/file/image-1.png", result.get(0).getUrl());
        assertEquals("https://cdn.example.com/medias/2/file/image-2.jpg", result.get(1).getUrl());
        verify(mediaRepository, times(1)).findAllById(ids);
        verify(mediaVmMapper, times(2)).toVm(any(Media.class));
    }

    @Test
    void getMediaByIds_whenIdsIsEmpty_thenReturnEmptyList() {
        // Given: caller passes an empty id list
        List<Long> ids = List.of();
        when(mediaRepository.findAllById(ids)).thenReturn(List.of());

        // When: getMediaByIds is invoked
        List<MediaVm> result = mediaService.getMediaByIds(ids);

        // Then: result is empty and mapper is never called
        assertTrue(result.isEmpty());
        verify(mediaRepository, times(1)).findAllById(ids);
        verify(mediaVmMapper, never()).toVm(any(Media.class));
    }

    @Test
    void getMediaByIds_whenSomeIdsDoNotExist_thenReturnOnlyExistingMappedItems() {
        // Given: repository returns only existing media for a mixed id input
        List<Long> ids = List.of(1L, 2L, 3L);
        Media media1 = buildMedia(1L, "image-1.png", "image/png", "caption-1");
        Media media3 = buildMedia(3L, "image-3.png", "image/png", "caption-3");
        when(mediaRepository.findAllById(ids)).thenReturn(List.of(media1, media3));
        when(mediaVmMapper.toVm(media1)).thenReturn(buildMediaVm(1L, "caption-1", "image-1.png", "image/png"));
        when(mediaVmMapper.toVm(media3)).thenReturn(buildMediaVm(3L, "caption-3", "image-3.png", "image/png"));
        when(yasConfig.publicUrl()).thenReturn("https://cdn.example.com");

        // When: getMediaByIds is invoked with partially missing ids
        List<MediaVm> result = mediaService.getMediaByIds(ids);

        // Then: only existing media are returned after mapping
        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals(3L, result.get(1).getId());
        verify(mediaRepository, times(1)).findAllById(ids);
        verify(mediaVmMapper, times(2)).toVm(any(Media.class));
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
