package org.example.gamestoreapp.service.impl;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import org.example.gamestoreapp.model.dto.EditProfileDTO;
import org.example.gamestoreapp.model.dto.UserDTO;
import org.example.gamestoreapp.model.view.UserProfileViewModel;
import org.example.gamestoreapp.model.entity.User;
import org.example.gamestoreapp.repository.UserRepository;
import org.example.gamestoreapp.service.UserService;
import org.example.gamestoreapp.service.session.UserHelperService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserHelperService userHelperService;
    private final ModelMapper modelMapper;

    @Value("${azure.storage.connection-string}")
    private String azureStorageConnectionString;

    public UserServiceImpl(UserRepository userRepository,
                           UserHelperService userHelperService,
                           ModelMapper modelMapper) {
        this.userRepository = userRepository;
        this.userHelperService = userHelperService;
        this.modelMapper = modelMapper;
    }

    @Override
    public Optional<UserDTO> getUserById(Long userId) {
        return userRepository.findById(userId)
                .map(user -> modelMapper.map(user, UserDTO.class));
    }

    @Override
    public EditProfileDTO getUserProfile() {
        User currentUser = userHelperService.getUser();

        EditProfileDTO editProfileDTO = new EditProfileDTO();
        editProfileDTO.setEmail(currentUser.getEmail());
        editProfileDTO.setFirstName(currentUser.getFirstName());
        editProfileDTO.setLastName(currentUser.getLastName());
        editProfileDTO.setAge(currentUser.getAge());

        return editProfileDTO;
    }

    @Override
    public void editProfile(EditProfileDTO editProfileDTO) {
        User currentUser = userHelperService.getUser();

        currentUser.setEmail(editProfileDTO.getEmail());
        currentUser.setFirstName(editProfileDTO.getFirstName());
        currentUser.setLastName(editProfileDTO.getLastName());
        currentUser.setAge(editProfileDTO.getAge());

        userRepository.save(currentUser);
    }

    @Override
    public UserProfileViewModel viewProfile() {
        User currentUser = userHelperService.getUser();
        UserProfileViewModel userProfileViewModel = new UserProfileViewModel();

        userProfileViewModel.setUsername(currentUser.getUsername());
        userProfileViewModel.setRole(currentUser.getRole().toString());
        userProfileViewModel.setAge(currentUser.getAge());
        userProfileViewModel.setEmail(currentUser.getEmail());
        userProfileViewModel.setFirstName(currentUser.getFirstName());
        userProfileViewModel.setLastName(currentUser.getLastName());
        userProfileViewModel.setProfileImageUrl(currentUser.getProfileImageUrl());

        return userProfileViewModel;
    }

    @Override
    public String uploadProfileImage(MultipartFile file, String containerName) throws IOException {

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
        String newFileName = username + "_" + System.currentTimeMillis() + extension;

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
