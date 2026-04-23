package com.yas.media.service;

import com.yas.commonlibrary.exception.NotFoundException;
import com.yas.media.config.YasConfig;
import com.yas.media.mapper.MediaVmMapper;
import com.yas.media.model.Media;
import com.yas.media.model.dto.MediaDto;
import com.yas.media.repository.FileSystemRepository;
import com.yas.media.repository.MediaRepository;
import com.yas.media.utils.StringUtils;
import com.yas.media.viewmodel.MediaPostVm;
import com.yas.media.viewmodel.MediaVm;
import com.yas.media.viewmodel.NoFileMediaVm;
import java.io.InputStream;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

@RequiredArgsConstructor
@Service
public class MediaServiceImpl implements MediaService {

    private final MediaVmMapper mediaVmMapper;
    private final MediaRepository mediaRepository;
    private final FileSystemRepository fileSystemRepository;
    private final YasConfig yasConfig;

    @Override
    @SneakyThrows
    public Media saveMedia(MediaPostVm mediaPostVm) {
        Media media = new Media();
        media.setCaption(mediaPostVm.caption());
        media.setMediaType(mediaPostVm.multipartFile().getContentType());

        if (StringUtils.hasText(mediaPostVm.fileNameOverride())) {
            media.setFileName(mediaPostVm.fileNameOverride().trim());
        } else {
            media.setFileName(mediaPostVm.multipartFile().getOriginalFilename());
        }
        String filePath = fileSystemRepository.persistFile(media.getFileName(),
            mediaPostVm.multipartFile().getBytes());
        media.setFilePath(filePath);

        return mediaRepository.save(media);
    }

    @Override
    public void removeMedia(Long id) {
        Media media = mediaRepository.findById(id).orElse(null);
        if (media == null) {
            throw new NotFoundException(String.format("Media %s is not found", id));
        }
        fileSystemRepository.deleteFile(media.getFilePath());
        mediaRepository.delete(media);
    }

    @Override
    public MediaVm getMediaById(Long id) {
        NoFileMediaVm noFileMediaVm = mediaRepository.findByIdWithoutFileInReturn(id);
        if (noFileMediaVm == null) {
            throw new NotFoundException(String.format("Media %d is not found", id));
        }
        String url = getMediaUrl(noFileMediaVm.id(), noFileMediaVm.fileName());

        return new MediaVm(
            noFileMediaVm.id(),
            noFileMediaVm.caption(),
            noFileMediaVm.fileName(),
            noFileMediaVm.mediaType(),
            url
        );
    }

    @Override
    public MediaDto getFile(Long id, String fileName) {
        Media media = mediaRepository.findById(id).orElse(null);
        if (media == null || !fileName.equalsIgnoreCase(media.getFileName())) {
            throw new NotFoundException(String.format("Media %d with fileName %s is not found", id, fileName));
        }
        MediaType mediaType = MediaType.valueOf(media.getMediaType());
        InputStream fileContent = fileSystemRepository.getFile(media.getFilePath());

        return MediaDto.builder()
            .content(fileContent)
            .mediaType(mediaType)
            .build();
    }

    @Override
    public List<MediaVm> getMediaByIds(List<Long> ids) {
        return mediaRepository.findAllById(ids).stream()
                .map(mediaVmMapper::toVm)
                .map(media -> {
                    String url = getMediaUrl(media.getId(), media.getFileName());
                    media.setUrl(url);
                    return media;
                }).toList();
    }

    private String getMediaUrl(Long mediaId, String fileName) {
        return UriComponentsBuilder.fromUriString(yasConfig.publicUrl())
                .path(String.format("/medias/%1$s/file/%2$s", mediaId, fileName))
                .build().toUriString();
    }
}