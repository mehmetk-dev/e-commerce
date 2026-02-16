package com.mehmetkerem.controller.impl;

import com.mehmetkerem.controller.IRestFileUploadController;
import com.mehmetkerem.exception.BadRequestException;
import com.mehmetkerem.service.IFileStorageService;
import com.mehmetkerem.util.ResultData;
import com.mehmetkerem.util.ResultHelper;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.Set;

@RestController
@RequestMapping("/v1/files")
@SuppressWarnings("null")
public class RestFileUploadControllerImpl implements IRestFileUploadController {

    private final IFileStorageService storageService;

    public RestFileUploadControllerImpl(IFileStorageService storageService) {
        this.storageService = storageService;
    }

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp");

    @Override
    @PostMapping("/upload")
    public ResultData<String> uploadFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            throw new BadRequestException("Dosya boş olamaz.");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new BadRequestException("Sadece resim dosyaları yüklenebilir (JPEG, PNG, GIF, WebP).");
        }
        String filename = storageService.save(file);

        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/v1/files/")
                .path(filename)
                .toUriString();

        return ResultHelper.success(fileDownloadUri);
    }

    @Override
    @GetMapping("/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> getFile(@PathVariable String filename) {
        Resource file = storageService.load(filename);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"")
                .body(file);
    }
}
