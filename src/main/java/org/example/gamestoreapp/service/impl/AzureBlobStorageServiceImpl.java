package org.example.gamestoreapp.service.impl;

import com.azure.storage.blob.BlobClientBuilder;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import org.example.gamestoreapp.service.AzureBlobStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;

@Service
public class AzureBlobStorageServiceImpl implements AzureBlobStorageService {

    @Value("${azure.storage.connection-string}")
    private String azureStorageConnectionString;

    @Override
    public String uploadFileFromLocal(String filePath, String containerName) {



        // Create the BlobContainerClient to interact with the container
        BlobContainerClient blobContainerClient = new BlobContainerClientBuilder()
                .connectionString(azureStorageConnectionString)
                .containerName(containerName)
                .buildClient();

        return "";
    }
}
