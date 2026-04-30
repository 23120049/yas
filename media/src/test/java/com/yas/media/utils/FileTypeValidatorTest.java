package com.yas.media.utils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.validation.ConstraintValidatorContext;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

class FileTypeValidatorTest {

    private static final String MESSAGE = "Invalid file type";

    private FileTypeValidator validator;
    private ConstraintValidatorContext context;
    private ConstraintValidatorContext.ConstraintViolationBuilder violationBuilder;

    @BeforeEach
    void setUp() {
        validator = new FileTypeValidator();

        ValidFileType annotation = mock(ValidFileType.class);
        when(annotation.allowedTypes()).thenReturn(new String[] {MediaType.IMAGE_PNG_VALUE});
        when(annotation.message()).thenReturn(MESSAGE);
        validator.initialize(annotation);

        context = mock(ConstraintValidatorContext.class);
        violationBuilder = mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);
        when(context.buildConstraintViolationWithTemplate(Mockito.anyString())).thenReturn(violationBuilder);
        when(violationBuilder.addConstraintViolation()).thenReturn(context);
    }

    @Test
    void isValid_whenFileIsNull_thenFalseAndAddsViolation() {
        boolean result = validator.isValid(null, context);

        assertFalse(result);
        verify(context).disableDefaultConstraintViolation();
        verify(context).buildConstraintViolationWithTemplate(MESSAGE);
        verify(violationBuilder).addConstraintViolation();
    }

    @Test
    void isValid_whenContentTypeIsNull_thenFalseAndAddsViolation() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getContentType()).thenReturn(null);

        boolean result = validator.isValid(file, context);

        assertFalse(result);
        verify(context).disableDefaultConstraintViolation();
        verify(context).buildConstraintViolationWithTemplate(MESSAGE);
        verify(violationBuilder).addConstraintViolation();
    }

    @Test
    void isValid_whenContentTypeNotAllowed_thenFalseAndAddsViolation() {
        MockMultipartFile file = new MockMultipartFile(
            "multipartFile",
            "test.jpg",
            MediaType.IMAGE_JPEG_VALUE,
            "dummy".getBytes()
        );

        boolean result = validator.isValid(file, context);

        assertFalse(result);
        verify(context).disableDefaultConstraintViolation();
        verify(context).buildConstraintViolationWithTemplate(MESSAGE);
        verify(violationBuilder).addConstraintViolation();
    }

    @Test
    void isValid_whenAllowedTypeAndValidImage_thenTrue() throws Exception {
        byte[] png = create1x1Png();
        MockMultipartFile file = new MockMultipartFile(
            "multipartFile",
            "test.png",
            MediaType.IMAGE_PNG_VALUE,
            png
        );

        boolean result = validator.isValid(file, context);

        assertTrue(result);
    }

    @Test
    void isValid_whenAllowedTypeButNotAnImage_thenFalse() {
        MockMultipartFile file = new MockMultipartFile(
            "multipartFile",
            "test.png",
            MediaType.IMAGE_PNG_VALUE,
            "not-an-image".getBytes()
        );

        boolean result = validator.isValid(file, context);

        assertFalse(result);
    }

    @Test
    void isValid_whenAllowedTypeButInputStreamThrowsIOException_thenFalse() throws Exception {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getContentType()).thenReturn(MediaType.IMAGE_PNG_VALUE);
        when(file.getInputStream()).thenThrow(new IOException("boom"));

        boolean result = validator.isValid(file, context);

        assertFalse(result);
    }

    private static byte[] create1x1Png() throws IOException {
        BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(image, "png", outputStream);
        return outputStream.toByteArray();
    }
}
