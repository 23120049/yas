package com.yas.media;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.yas.media.config.FilesystemConfig;
import com.yas.media.repository.FileSystemRepository;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FileSystemRepositoryTest {

    @Mock
    private FilesystemConfig filesystemConfig;

    private FileSystemRepository fileSystemRepository;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        fileSystemRepository = new FileSystemRepository(filesystemConfig);
    }

    @Test
    void persistFile_whenDirectoryNotExist_thenThrowsException() {
        when(filesystemConfig.getDirectory()).thenReturn(tempDir.resolve("non_existent").toString());
        
        assertThrows(IllegalStateException.class, () -> fileSystemRepository.persistFile("test.png", new byte[0]));
    }

    @Test
    void persistFile_whenDirectoryNotAccessible_thenThrowsException() {
        when(filesystemConfig.getDirectory()).thenReturn(tempDir.toString());
        File dir = tempDir.toFile();
        
        boolean writable = dir.canWrite();
        dir.setWritable(false);
        
        try {
            // Mấu chốt: Chỉ chạy lệnh assertThrows nếu việc chặn quyền ghi THỰC SỰ có tác dụng.
            // Trên máy Windows của bạn, lệnh này sẽ "bỏ qua" test một cách êm đẹp.
            // Lên GitHub (Linux), lệnh này sẽ chạy và pass mượt mà.
            org.junit.jupiter.api.Assumptions.assumeTrue(!dir.canWrite(), 
                    "Bỏ qua test này trên Windows vì HĐH không hỗ trợ chặn quyền ghi triệt để");
            
            assertThrows(IllegalStateException.class, () -> fileSystemRepository.persistFile("test.png", new byte[0]));
        } finally {
            dir.setWritable(writable);
        }
    }

    @Test
    void persistFile_whenFilenameHasTraversal_thenThrowsIllegalArgumentException() {
        when(filesystemConfig.getDirectory()).thenReturn(tempDir.toString());

        assertThrows(IllegalArgumentException.class, () -> fileSystemRepository.persistFile("../evil.png", new byte[0]));
        assertThrows(IllegalArgumentException.class, () -> fileSystemRepository.persistFile("a/b.png", new byte[0]));
        assertThrows(IllegalArgumentException.class, () -> fileSystemRepository.persistFile("a\\b.png", new byte[0]));
    }

    @Test
    void persistFile_whenDirectoryOk_thenWritesFileAndReturnsPath() throws IOException {
        when(filesystemConfig.getDirectory()).thenReturn(tempDir.toString());
        String filename = "file.png";
        byte[] content = "hello".getBytes();

        String result = fileSystemRepository.persistFile(filename, content);

        Path expectedPath = tempDir.resolve(filename).toAbsolutePath().normalize();
        assertEquals(expectedPath.toString(), result);
        assertTrue(Files.exists(expectedPath));
        assertArrayEquals(content, Files.readAllBytes(expectedPath));
    }

    @Test
    void getFile_whenFileDoesNotExist_thenThrowsException() {
        when(filesystemConfig.getDirectory()).thenReturn(tempDir.toString());
        String missingFilePath = tempDir.resolve("missing.png").toString();

        assertThrows(IllegalStateException.class, () -> fileSystemRepository.getFile(missingFilePath));
    }

    @Test
    void getFile_whenFileExists_thenReturnsStream() throws IOException {
        String filename = "ok.png";
        byte[] content = "data".getBytes();
        Path file = tempDir.resolve(filename);
        Files.write(file, content);
        
        try (InputStream is = fileSystemRepository.getFile(file.toString())) {
            assertArrayEquals(content, is.readAllBytes());
        }
    }
}
