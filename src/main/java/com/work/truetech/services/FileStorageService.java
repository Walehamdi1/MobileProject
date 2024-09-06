package com.work.truetech.services;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class FileStorageService {
    @Async
    public void saveFile(MultipartFile multipartFile, String fileName, String uploadPath) throws IOException {
        Path upload = Paths.get(uploadPath);
        if (!Files.exists(upload)) {
            Files.createDirectories(upload);
        }
        try (InputStream inputStream = multipartFile.getInputStream()) {
            Path filePath = upload.resolve(fileName);
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new IOException("Could not save file", e);
        }
    }
}
