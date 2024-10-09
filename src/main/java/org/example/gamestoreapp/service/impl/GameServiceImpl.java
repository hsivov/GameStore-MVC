package org.example.gamestoreapp.service.impl;

import org.example.gamestoreapp.model.dto.GameDTO;
import org.example.gamestoreapp.model.entity.Game;
import org.example.gamestoreapp.model.entity.User;
import org.example.gamestoreapp.repository.GameRepository;
import org.example.gamestoreapp.repository.UserRepository;
import org.example.gamestoreapp.service.GameService;
import org.example.gamestoreapp.service.session.UserHelperService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class GameServiceImpl implements GameService {
    private final GameRepository gameRepository;
    private final ModelMapper modelMapper;
    private final UserHelperService userHelperService;
    private final UserRepository userRepository;

    public GameServiceImpl(GameRepository gameRepository, ModelMapper modelMapper, UserHelperService userHelperService, UserRepository userRepository) {
        this.gameRepository = gameRepository;
        this.modelMapper = modelMapper;
        this.userHelperService = userHelperService;
        this.userRepository = userRepository;
    }

    @Override
    public List<GameDTO> getAll() {
        return gameRepository.findAll().stream()
                .map(game -> modelMapper.map(game, GameDTO.class))
                .toList();
    }

    @Override
    public List<GameDTO> getOwnedGames() {
        User currentUser = userHelperService.getUser();

        return currentUser.getOwnedGames().stream()
                .map((game) -> modelMapper.map(game, GameDTO.class))
                .sorted(Comparator.comparing(GameDTO::getTitle))
                .toList();
    }

    @Override
    public void addToLibrary(Long id) {
        User currentUser = userHelperService.getUser();
        Optional<Game> game = gameRepository.findById(id);

        if (game.isPresent()) {
            Game gameToAdd = game.get();
            currentUser.getOwnedGames().add(gameToAdd);

            userRepository.save(currentUser);
        }
    }

    @Override
    public GameDTO getGameById(Long id) {
        Optional<Game> optionalGame = gameRepository.findById(id);

        return optionalGame.map(game -> modelMapper.map(game, GameDTO.class)).orElse(null);
    }
}
