package com.e_commerce.e_commerce.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class ProductVariantImageService {
    Cloudinary cloudinary;

    public String uploadImage(MultipartFile file) throws IOException {
        assert file.getOriginalFilename() != null;
        String publicValue = generatePublicValue(file.getOriginalFilename());
        log.info("publicValue: {}", publicValue);
        String extension = getFileName(file.getOriginalFilename())[1];
        log.info("extension: {}", extension);
        File fileToUpload = convert(file, publicValue, extension);
        log.info("fileToUpload: {}", fileToUpload);
        // upload file to Cloudinary
        cloudinary.uploader().upload(fileToUpload, ObjectUtils.asMap("public_id", publicValue));

        cleanDisk(fileToUpload);

        // create public url for accessing
        return cloudinary.url().generate(StringUtils.join(publicValue, ".", extension));
    }

    private File convert(MultipartFile file, String publicValue, String extension) throws IOException {
        assert file.getOriginalFilename() != null;
        // join publicValue and extension (extension like jpg, png,...)
        File convFile = new File(StringUtils.join(publicValue, ".", extension));
        try (InputStream is = file.getInputStream()) {
            Files.copy(is, convFile.toPath());
        }
        return convFile;
    }

    private void cleanDisk(File file) {
        try {
            Path filePath = file.toPath();
            log.info("filePath: {}", filePath);
            Files.delete(filePath);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    public String generatePublicValue(String originalName) {
        String fileName = getFileName(originalName)[0];
        return StringUtils.join(UUID.randomUUID().toString(), "_", fileName);
    }

    public String[] getFileName(String originalName) {
        return originalName.split("\\.");
    }
}
