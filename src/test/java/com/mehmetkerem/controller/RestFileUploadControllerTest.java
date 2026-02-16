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
    @DisplayName("getFile - Resource ve attachment header döner")
    void getFile_ShouldReturnResource() {
        Resource resource = mock(Resource.class);
        when(resource.getFilename()).thenReturn("file.pdf");
        when(storageService.load("file.pdf")).thenReturn(resource);

        ResponseEntity<Resource> response = controller.getFile("file.pdf");

        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertNotNull(response.getBody());
        String contentDisposition = response.getHeaders().getFirst("Content-Disposition");
        assertNotNull(contentDisposition);
        assertTrue(contentDisposition.contains("attachment"));
        assertTrue(contentDisposition.contains("file.pdf"));
        verify(storageService).load("file.pdf");
    }
}
