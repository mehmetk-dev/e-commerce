package com.mehmetkerem.controller;

import com.mehmetkerem.util.ResultData;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IRestFileUploadController {
    ResultData<String> uploadFile(MultipartFile file);

    ResultData<List<String>> uploadMultiple(List<MultipartFile> files);

    ResponseEntity<Resource> getFile(String filename);
}
