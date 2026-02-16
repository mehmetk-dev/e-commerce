package com.mehmetkerem.config;

import com.mehmetkerem.service.IFileStorageService;
import jakarta.annotation.Resource;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FileStorageConfig implements CommandLineRunner {

    @Resource
    IFileStorageService storageService;

    @Override
    public void run(String... arg) throws Exception {
        storageService.init();
    }
}
