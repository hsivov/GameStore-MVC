package org.example.gamestoreapp.service.impl;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import org.example.gamestoreapp.model.dto.*;
import org.example.gamestoreapp.model.entity.Game;
import org.example.gamestoreapp.model.entity.Genre;
import org.example.gamestoreapp.model.entity.User;
import org.example.gamestoreapp.model.enums.UserRole;
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

    @Value("${azure.storage.connection-string}")
    private String azureStorageConnectionString;


    public AdminServiceImpl(ModelMapper modelMapper, GameRepository gameRepository, UserRepository userRepository, GenreRepository genreRepository) {
        this.modelMapper = modelMapper;
        this.gameRepository = gameRepository;
        this.userRepository = userRepository;
        this.genreRepository = genreRepository;
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
    public void demote(long id) {
        Optional<User> user = userRepository.findById(id);

        if (user.isPresent()) {
            User userEntity = user.get();
            userEntity.setRole(UserRole.USER);

            userRepository.save(userEntity);
        }
    }

    @Override
    public void toggleUserState(long id) {
        Optional<User> byId = userRepository.findById(id);

        if (byId.isPresent()) {
            User user = byId.get();
            user.setEnabled(!user.isEnabled());
            userRepository.save(user);
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
        Game game = gameRepository.findById(id)
                .orElse(null);
        return modelMapper.map(game, UpdateGameBindingModel.class);
    }

    @Override
    public void editGame(UpdateGameBindingModel updateGameBindingModel) throws IOException {
        Optional<Game> optionalGame = gameRepository.findById(updateGameBindingModel.getId());

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

    @Override
    public List<GenreDTO> getAllGenres() {
        return genreRepository.findAll().stream()
                .map(genre -> modelMapper.map(genre, GenreDTO.class))
                .toList();
    }

    @Override
    public void addGenre(AddGenreBindingModel addGenreBindingModel) {
        Genre genre = new Genre();
        genre.setName(addGenreBindingModel.getName());
        genre.setDescription(addGenreBindingModel.getDescription());

        genreRepository.save(genre);
    }

    @Override
    public UpdateGenreBindingModel getGenreById(long id) {
        Optional<Genre> byId = genreRepository.findById(id);

        if (byId.isPresent()) {
            Genre genre = byId.get();
            return modelMapper.map(genre, UpdateGenreBindingModel.class);
        }

        return null;
    }

    @Override
    public void editGenre(UpdateGenreBindingModel updateGenreBindingModel) {
        long id = updateGenreBindingModel.getId();
        Optional<Genre> byId = genreRepository.findById(id);

        if (byId.isPresent()) {
            Genre genre = byId.get();
            genre.setName(updateGenreBindingModel.getName());
            genre.setDescription(updateGenreBindingModel.getDescription());
            genreRepository.save(genre);
        }
    }

    @Override
    public void deleteGenre(Long id) {
        genreRepository.deleteById(id);
    }
}
