package org.example.gamestoreapp.service.impl;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import com.azure.storage.blob.models.BlobHttpHeaders;
import jakarta.transaction.Transactional;
import org.apache.tika.Tika;
import org.example.gamestoreapp.model.dto.AddGameBindingModel;
import org.example.gamestoreapp.model.dto.UpdateGameBindingModel;
import org.example.gamestoreapp.model.dto.GameDTO;
import org.example.gamestoreapp.model.dto.UserDTO;
import org.example.gamestoreapp.model.entity.Game;
import org.example.gamestoreapp.model.entity.Genre;
import org.example.gamestoreapp.model.entity.User;
import org.example.gamestoreapp.model.enums.UserRole;
import org.example.gamestoreapp.repository.ConfirmationTokenRepository;
import org.example.gamestoreapp.repository.GameRepository;
import org.example.gamestoreapp.repository.GenreRepository;
import org.example.gamestoreapp.repository.UserRepository;
import org.example.gamestoreapp.service.AdminService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Paths;
import java.util.*;

@Service
public class AdminServiceImpl implements AdminService {
    private final ModelMapper modelMapper;
    private final GameRepository gameRepository;
    private final UserRepository userRepository;
    private final GenreRepository genreRepository;
    private final ConfirmationTokenRepository confirmationTokenRepository;

    @Value("${azure.storage.connection-string}")
    private String azureStorageConnectionString;


    public AdminServiceImpl(ModelMapper modelMapper, GameRepository gameRepository, UserRepository userRepository, GenreRepository genreRepository, ConfirmationTokenRepository confirmationTokenRepository) {
        this.modelMapper = modelMapper;
        this.gameRepository = gameRepository;
        this.userRepository = userRepository;
        this.genreRepository = genreRepository;
        this.confirmationTokenRepository = confirmationTokenRepository;
    }

    @Override
    public void addGame(AddGameBindingModel addGameBindingModel) throws IOException {
        Game game = new Game();
        Genre genre = genreRepository.findByName(addGameBindingModel.getGenre());

        String imageBlobUrl = uploadToAzureBlobStorage(addGameBindingModel.getImageUrl(), "images");
        String videoBlobUrl = uploadToAzureBlobStorage(addGameBindingModel.getVideoUrl(), "videos");

        game.setTitle(addGameBindingModel.getTitle());
        game.setDescription(addGameBindingModel.getDescription());
        game.setImageUrl(imageBlobUrl);
        game.setVideoUrl(videoBlobUrl);
        game.setPublisher(addGameBindingModel.getPublisher());
        game.setReleaseDate(addGameBindingModel.getReleaseDate());
        game.setPrice(addGameBindingModel.getPrice());
        game.setGenre(genre);

        gameRepository.save(game);
    }

    private String uploadToAzureBlobStorage(String fileUrl, String containerName) throws IOException {
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

            // Use Apache Tika to determine the content type dynamically
            Tika tika = new Tika();
            String detectedContentType = tika.detect(data);

            // Optionally, set the correct content type (e.g., image/jpeg)
            BlobHttpHeaders headers = new BlobHttpHeaders().setContentType(detectedContentType);
            blobClient.setHttpHeaders(headers);
        }

        // Return the URL of the uploaded image
        return blobClient.getBlobUrl();
    }

    @Override
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map((user) -> modelMapper.map(user, UserDTO.class))
                .toList();
    }

    @Override
    public void promote(long id) {
        Optional<User> user = userRepository.findById(id);

        if (user.isPresent()) {
            User userEntity = user.get();
            userEntity.setRole(UserRole.ADMIN);

            userRepository.save(userEntity);
        }
    }

    @Override
    @Transactional
    public void deleteUser(long id) {
        confirmationTokenRepository.deleteByUserId(id);

        userRepository.deleteById(id);
    }

    @Override
    public void demote(long id) {
        Optional<User> user = userRepository.findById(id);

        if (user.isPresent()) {
            User userEntity = user.get();
            userEntity.setRole(UserRole.USER);

            userRepository.save(userEntity);
        }
    }

    @Override
    public List<GameDTO> getAllGames() {
        return gameRepository.findAll().stream()
                .map((game) -> modelMapper.map(game, GameDTO.class))
                .toList();
    }

    @Override
    public void deleteGame(Long id) {
        gameRepository.deleteById(id);
    }

    @Override
    public UpdateGameBindingModel getById(Long id) {
        return modelMapper.map(gameRepository.findById(id).orElse(null), UpdateGameBindingModel.class);
    }

    @Override
    public void editGame(UpdateGameBindingModel updateGameBindingModel, Long id) throws IOException {
        Optional<Game> optionalGame = gameRepository.findById(id);

        if (optionalGame.isPresent()) {
            Game game = optionalGame.get();
            Genre genre = genreRepository.findByName(updateGameBindingModel.getGenre());
            String imageBlobUrl = updateGameBindingModel.getImageUrl();
            String videoBlobUrl = updateGameBindingModel.getVideoUrl();

            if (!imageBlobUrl.equals(game.getImageUrl())) {
                imageBlobUrl = uploadToAzureBlobStorage(updateGameBindingModel.getImageUrl(), "images");
            }

            if (!videoBlobUrl.equals(game.getVideoUrl())) {
                videoBlobUrl = uploadToAzureBlobStorage(updateGameBindingModel.getVideoUrl(), "videos");
            }

            game.setTitle(updateGameBindingModel.getTitle());
            game.setDescription(updateGameBindingModel.getDescription());
            game.setImageUrl(imageBlobUrl);
            game.setVideoUrl(videoBlobUrl);
            game.setPublisher(updateGameBindingModel.getPublisher());
            game.setReleaseDate(updateGameBindingModel.getReleaseDate());
            game.setPrice(updateGameBindingModel.getPrice());
            game.setGenre(genre);

            gameRepository.save(game);
        }
    }
}
