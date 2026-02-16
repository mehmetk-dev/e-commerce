package com.mehmetkerem.service.impl;

import com.mehmetkerem.exception.BaseException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class FileSystemStorageServiceTest {

    @TempDir
    Path tempDir;

    private FileSystemStorageService storageService;
    private Path root;

    @BeforeEach
    void setUp() {
        root = tempDir.resolve("uploads");
        storageService = new FileSystemStorageService();
        ReflectionTestUtils.setField(storageService, "root", root);
    }

    @AfterEach
    void tearDown() {
        if (storageService != null) {
            try {
                storageService.deleteAll();
            } catch (Exception ignored) {}
        }
    }

    @Test
    @DisplayName("init - klasör yoksa oluşturulur")
    void init_WhenNotExists_ShouldCreateDirectory() {
        storageService.init();
        assertTrue(Files.exists(root));
    }

    @Test
    @DisplayName("init - klasör varsa hata vermez")
    void init_WhenExists_ShouldNotThrow() {
        storageService.init();
        storageService.init();
        assertTrue(Files.exists(root));
    }

    @Test
    @DisplayName("save - dosya kaydedilir ve benzersiz isim döner")
    void save_ShouldStoreFileAndReturnFilename() {
        storageService.init();
        MultipartFile file = new MockMultipartFile("file", "original.jpg", "image/jpeg", new byte[] { 1, 2, 3 });

        String filename = storageService.save(file);

        assertNotNull(filename);
        assertTrue(filename.endsWith(".jpg"));
        assertTrue(Files.exists(root.resolve(filename)));
    }

    @Test
    @DisplayName("save - uzantısız dosya da kaydedilir")
    void save_NoExtension_ShouldStillSave() {
        storageService.init();
        MultipartFile file = new MockMultipartFile("file", "noext", "text/plain", new byte[] { 1 });

        String filename = storageService.save(file);

        assertNotNull(filename);
        assertTrue(Files.exists(root.resolve(filename)));
    }

    @Test
    @DisplayName("load - mevcut dosya Resource döner")
    void load_WhenFileExists_ShouldReturnResource() throws IOException {
        storageService.init();
        String name = "test.txt";
        Files.write(root.resolve(name), "hello".getBytes());

        Resource resource = storageService.load(name);

        assertNotNull(resource);
        assertTrue(resource.exists());
    }

    @Test
    @DisplayName("load - olmayan dosya BaseException")
    void load_WhenFileNotExists_ShouldThrow() {
        storageService.init();
        assertThrows(BaseException.class, () -> storageService.load("nonexistent.txt"));
    }

    @Test
    @DisplayName("load - path traversal reddedilir")
    void load_WhenPathTraversal_ShouldThrow() {
        storageService.init();
        assertThrows(BaseException.class, () -> storageService.load("../../../etc/passwd"));
        assertThrows(BaseException.class, () -> storageService.load("..\\..\\etc\\passwd"));
    }

    @Test
    @DisplayName("loadAll - boş dizin boş stream")
    void loadAll_WhenEmpty_ReturnsEmptyStream() {
        storageService.init();
        Stream<Path> stream = storageService.loadAll();
        assertNotNull(stream);
        assertEquals(0, stream.count());
    }

    @Test
    @DisplayName("deleteAll - dizin silinir")
    void deleteAll_ShouldRemoveRootContents() throws IOException {
        storageService.init();
        Files.write(root.resolve("a.txt"), new byte[] { 1 });
        assertTrue(Files.exists(root.resolve("a.txt")));
        storageService.deleteAll();
        assertFalse(Files.exists(root.resolve("a.txt")));
    }
}
