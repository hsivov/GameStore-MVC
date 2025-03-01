package org.example.gamestoreapp.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface AzureBlobStorageService {
    String uploadToAzureBlobStorage(String fileUrl, String containerName) throws IOException;

    String uploadToAzureBlobStorage(MultipartFile file, String containerName) throws IOException;
}
