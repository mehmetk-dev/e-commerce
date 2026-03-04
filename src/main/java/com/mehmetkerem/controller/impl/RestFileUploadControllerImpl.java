package com.mehmetkerem.controller.impl;

import com.mehmetkerem.controller.IRestFileUploadController;
import com.mehmetkerem.exception.BadRequestException;
import com.mehmetkerem.service.IFileStorageService;
import com.mehmetkerem.util.ResultData;
import com.mehmetkerem.util.ResultHelper;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
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
        validateImage(file);
        String cloudinaryUrl = storageService.save(file);
        return ResultHelper.success(cloudinaryUrl);
    }

    @Override
    @PostMapping("/upload-multiple")
    public ResultData<List<String>> uploadMultiple(
            @RequestParam("files") List<MultipartFile> files) {

        if (files == null || files.isEmpty()) {
            throw new BadRequestException("En az bir dosya gerekli.");
        }
        if (files.size() > 10) {
            throw new BadRequestException("Tek seferde en fazla 10 dosya yüklenebilir.");
        }

        List<String> urls = new ArrayList<>();
        for (MultipartFile file : files) {
            validateImage(file);
            urls.add(storageService.save(file));
        }
        return ResultHelper.success(urls);
    }

    @Override
    @GetMapping("/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> getFile(@PathVariable String filename) {
        return ResponseEntity.status(HttpStatus.GONE).build();
    }

    private void validateImage(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BadRequestException("Dosya boş olamaz.");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new BadRequestException("Sadece resim dosyaları yüklenebilir (JPEG, PNG, GIF, WebP).");
        }
    }
}
