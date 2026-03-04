package com.mehmetkerem.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.mehmetkerem.exception.BadRequestException;
import com.mehmetkerem.service.IFileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@Slf4j
@Service
@Primary
@RequiredArgsConstructor
public class CloudinaryStorageService implements IFileStorageService {

    private final Cloudinary cloudinary;

    @Value("${app.cloudinary.folder:can-antika}")
    private String cloudinaryFolder;

    @Value("${app.cloudinary.upload-preset:canantika}")
    private String uploadPreset;

    @Override
    public void init() {
        // Cloudinary için init gerekmez
    }

    @Override
    @SuppressWarnings("unchecked")
    public String save(MultipartFile file) {
        try {
            String publicId = cloudinaryFolder + "/" + UUID.randomUUID();

            Map<String, Object> result = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "public_id", publicId,
                            "upload_preset", uploadPreset,
                            "resource_type", "image",
                            "overwrite", false));

            String url = (String) result.get("secure_url");
            log.info("Cloudinary upload OK: {}", url);
            return url;

        } catch (IOException e) {
            log.error("Cloudinary upload failed", e);
            throw new BadRequestException("Dosya yüklenemedi: " + e.getMessage());
        }
    }

    @Override
    public Resource load(String filename) {
        // Cloudinary URL'leri direkt kullanılır, bu metod legacy uyumluluk için
        throw new UnsupportedOperationException("Cloudinary dosyaları URL üzerinden erişilir.");
    }

    @Override
    public void deleteAll() {
        // Cloudinary'de toplu silme admin API gerektirir, şimdilik no-op
        log.warn("deleteAll() Cloudinary'de desteklenmiyor.");
    }

    @Override
    public Stream<Path> loadAll() {
        return Stream.empty();
    }

    @Override
    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank())
            return;
        try {
            // Regex to extract public_id from Cloudinary URL
            Pattern pattern = Pattern.compile(".*upload/(?:v\\d+/)?(.+)\\.[^.]+$");
            Matcher matcher = pattern.matcher(fileUrl);
            if (matcher.matches()) {
                String publicId = matcher.group(1);
                delete(publicId);
            } else {
                log.warn("Could not extract publicId from URL: {}", fileUrl);
            }
        } catch (Exception e) {
            log.error("Error parsing Cloudinary URL: {}", fileUrl, e);
        }
    }

    /**
     * Cloudinary'den dosya sil (public_id ile).
     * Ürün silindiğinde çağrılabilir.
     */
    public void delete(String publicId) {
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            log.info("Cloudinary delete OK: {}", publicId);
        } catch (IOException e) {
            log.error("Cloudinary delete failed: {}", publicId, e);
        }
    }
}
