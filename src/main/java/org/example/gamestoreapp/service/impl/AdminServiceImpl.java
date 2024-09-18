package org.example.gamestoreapp.service.impl;

import org.example.gamestoreapp.model.dto.AddGameBindingModel;
import org.example.gamestoreapp.model.dto.UpdateGameBindingModel;
import org.example.gamestoreapp.model.dto.GameDTO;
import org.example.gamestoreapp.model.dto.UserDTO;
import org.example.gamestoreapp.model.entity.Game;
import org.example.gamestoreapp.model.entity.Genre;
import org.example.gamestoreapp.model.entity.User;
import org.example.gamestoreapp.model.enums.UserRole;
import org.example.gamestoreapp.repository.GameRepository;
import org.example.gamestoreapp.repository.GenreRepository;
import org.example.gamestoreapp.repository.UserRepository;
import org.example.gamestoreapp.service.AdminService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AdminServiceImpl implements AdminService {
    private final ModelMapper modelMapper;
    private final GameRepository gameRepository;
    private final UserRepository userRepository;
    private final GenreRepository genreRepository;

    public AdminServiceImpl(ModelMapper modelMapper, GameRepository gameRepository, UserRepository userRepository, GenreRepository genreRepository) {
        this.modelMapper = modelMapper;
        this.gameRepository = gameRepository;
        this.userRepository = userRepository;
        this.genreRepository = genreRepository;
    }

    @Override
    public void addGame(AddGameBindingModel addGameBindingModel) {
        Game game = new Game();
        Genre genre = genreRepository.findByName(addGameBindingModel.getGenre());

        game.setTitle(addGameBindingModel.getTitle());
        game.setDescription(addGameBindingModel.getDescription());
        game.setImageUrl(addGameBindingModel.getImageUrl());
        game.setPublisher(addGameBindingModel.getPublisher());
        game.setReleaseDate(addGameBindingModel.getReleaseDate());
        game.setPrice(addGameBindingModel.getPrice());
        game.setGenre(genre);

        gameRepository.save(game);
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
    public void deleteUser(long id) {
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
    public void editGame(UpdateGameBindingModel updateGameBindingModel, Long id) {
        Optional<Game> optionalGame = gameRepository.findById(id);

        if (optionalGame.isPresent()) {
            Game game = optionalGame.get();
            Genre genre = genreRepository.findByName(updateGameBindingModel.getGenre());

            game.setTitle(updateGameBindingModel.getTitle());
            game.setDescription(updateGameBindingModel.getDescription());
            game.setImageUrl(updateGameBindingModel.getImageUrl());
            game.setPublisher(updateGameBindingModel.getPublisher());
            game.setReleaseDate(updateGameBindingModel.getReleaseDate());
            game.setPrice(updateGameBindingModel.getPrice());
            game.setGenre(genre);

            gameRepository.save(game);
        }
    }
}
