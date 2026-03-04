package com.mehmetkerem.controller;

import com.mehmetkerem.controller.impl.RestFileUploadControllerImpl;
import com.mehmetkerem.service.IFileStorageService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RestFileUploadControllerTest {

    @Mock
    private IFileStorageService storageService;

    @InjectMocks
    private RestFileUploadControllerImpl controller;

    @Test
    @DisplayName("getFile - Cloudinary'ye geçildiği için artık local dosya sunulmaz (410 GONE)")
    void getFile_ShouldReturnGoneStatus() {
        ResponseEntity<Resource> response = controller.getFile("test.jpg");
        assertEquals(410, response.getStatusCode().value());
        assertNull(response.getBody());
    }
}
