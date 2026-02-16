package com.mehmetkerem.controller;

import com.mehmetkerem.util.ResultData;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

public interface IRestFileUploadController {
    ResultData<String> uploadFile(MultipartFile file);

    ResponseEntity<Resource> getFile(String filename);
}
