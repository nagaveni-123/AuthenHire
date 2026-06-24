package com.authenhire.authenhire.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileStorageService {

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    public String save(MultipartFile file, String subFolder) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }

        Path base = Paths.get(uploadDir, subFolder);
        Files.createDirectories(base);

        String originalName = file.getOriginalFilename() == null ? "file" : file.getOriginalFilename();
        String safeName = UUID.randomUUID() + "_" + originalName.replaceAll("\\s+", "_");

        Path target = base.resolve(safeName);
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

        return "/uploads/" + subFolder + "/" + safeName;
    }
}
