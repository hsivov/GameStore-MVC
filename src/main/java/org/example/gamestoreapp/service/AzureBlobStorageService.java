package org.example.gamestoreapp.service;

import java.net.MalformedURLException;

public interface AzureBlobStorageService {
    String uploadFileFromLocal(String filePath, String containerName);
}
