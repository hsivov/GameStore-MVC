package org.example.gamestoreapp.service.impl;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.example.gamestoreapp.model.entity.User;
import org.example.gamestoreapp.repository.UserRepository;
import org.example.gamestoreapp.service.AzureBlobStorageService;
import org.example.gamestoreapp.service.session.UserHelperService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class AzureBlobStorageServiceImpl implements AzureBlobStorageService {
    private static final Logger logger = LoggerFactory.getLogger(AzureBlobStorageServiceImpl.class);
    private final UserHelperService userHelperService;
    private final UserRepository userRepository;

    @Value("${azure.storage.connection-string}")
    private String azureStorageConnectionString;

    public AzureBlobStorageServiceImpl(UserHelperService userHelperService, UserRepository userRepository) {
        this.userHelperService = userHelperService;
        this.userRepository = userRepository;
    }

    @Override
    public String uploadToAzureBlobStorage(String fileUrl, String containerName) throws IOException {
        try {
            URL url = new URL(fileUrl);
            String uuidShort= UUID.randomUUID().toString().substring(0, 8);

            // Generate a unique filename based on the original URL or a UUID
            String originalFileName = Paths.get(url.getPath()).getFileName().toString();
            String uniqueFileName = uuidShort + "_" + originalFileName;

            // Create the BlobContainerClient to interact with the container
            BlobContainerClient blobContainerClient = new BlobContainerClientBuilder()
                    .connectionString(azureStorageConnectionString)
                    .containerName(containerName)
                    .buildClient();

            // Get a reference to the BlobClient for the unique file
            BlobClient blobClient = blobContainerClient.getBlobClient(uniqueFileName);

            // Download the image from the provided URL
            try (InputStream inputStream = url.openStream()) {
                // Read the input stream into a byte array
                byte[] data = inputStream.readAllBytes();

                // Upload the image to Azure Blob Storage
                blobClient.upload(new ByteArrayInputStream(data), data.length, true);
            }

            // Return the URL of the uploaded image
            return blobClient.getBlobUrl();
        } catch (IOException e) {
            logger.error("Failed to upload file to Azure Blob Storage: {}", fileUrl, e);
            throw new FileUploadException("Failed to upload file to Azure Blob Storage: " + fileUrl, e);
        }
    }

    @Override
    public String uploadToAzureBlobStorage(MultipartFile file, String containerName) throws IOException {
        String originalFileName = Objects.requireNonNull(file.getOriginalFilename());

        List<String> allowedExtensions = Arrays.asList("jpg", "jpeg", "png");

        boolean isValidExtension = allowedExtensions.stream()
                .anyMatch(ext -> originalFileName.toLowerCase().endsWith(ext));

        if (!isValidExtension) {
            throw new IllegalArgumentException("Invalid file type: only .jpg, .jpeg, and .png files are allowed.");
        }
        // Get currently logged user
        User currentUser = userHelperService.getUser();

        String extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        String username = currentUser.getUsername();
        long timeStamp = System.currentTimeMillis();

        String newFileName = username + "_" + timeStamp + extension;

        // Create the BlobContainerClient to interact with the container
        BlobContainerClient blobContainerClient = new BlobContainerClientBuilder()
                .connectionString(azureStorageConnectionString)
                .containerName(containerName)
                .buildClient();

        // Ensure the container exists
        if (!blobContainerClient.exists()) {
            blobContainerClient.create();
        }

        // Get the blob client for the file
        BlobClient blobClient = blobContainerClient.getBlobClient(newFileName);

        // Upload the file
        blobClient.upload(file.getInputStream(), file.getSize(), true);

        // Get the file blob url and save
        currentUser.setProfileImageUrl(blobClient.getBlobUrl());
        userRepository.save(currentUser);

        return blobClient.getBlobUrl();
    }
}
