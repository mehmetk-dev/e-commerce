package com.mehmetkerem.service.impl;

import com.mehmetkerem.exception.BaseException;
import com.mehmetkerem.service.IFileStorageService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.stream.Stream;

@Service
@SuppressWarnings("null")
public class FileSystemStorageService implements IFileStorageService {

    private final Path root = Paths.get("uploads");

    @Override
    public void init() {
        try {
            if (!Files.exists(root)) {
                Files.createDirectory(root);
            }
        } catch (IOException e) {
            throw new BaseException("Could not initialize folder for upload!", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public String save(MultipartFile file) {
        try {
            // Generate unique filename to prevent overwrites
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String newFilename = UUID.randomUUID().toString() + extension;

            Files.copy(file.getInputStream(), this.root.resolve(newFilename));
            return newFilename;
        } catch (Exception e) {
            throw new BaseException("Could not store the file. Error: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public Resource load(String filename) {
        try {
            if (filename == null || filename.isBlank() || filename.contains("..")) {
                throw new BaseException("Geçersiz dosya adı.", HttpStatus.BAD_REQUEST);
            }
            Path file = root.resolve(filename).normalize();
            Path rootAbs = root.toAbsolutePath().normalize();
            if (!file.startsWith(rootAbs)) {
                throw new BaseException("Geçersiz dosya yolu.", HttpStatus.BAD_REQUEST);
            }
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() && resource.isReadable()) {
                return resource;
            }
            throw new BaseException("Could not read the file!", HttpStatus.NOT_FOUND);
        } catch (MalformedURLException e) {
            throw new BaseException("Error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public void deleteAll() {
        FileSystemUtils.deleteRecursively(root.toFile());
    }

    @Override
    public Stream<Path> loadAll() {
        try {
            return Files.walk(this.root, 1).filter(path -> !path.equals(this.root)).map(this.root::relativize);
        } catch (IOException e) {
            throw new BaseException("Could not load the files!", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
