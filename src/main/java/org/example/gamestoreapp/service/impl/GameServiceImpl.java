package org.example.gamestoreapp.service.impl;

import org.example.gamestoreapp.model.dto.GameDTO;
import org.example.gamestoreapp.model.entity.Game;
import org.example.gamestoreapp.model.entity.User;
import org.example.gamestoreapp.repository.GameRepository;
import org.example.gamestoreapp.repository.UserRepository;
import org.example.gamestoreapp.service.ApiService;
import org.example.gamestoreapp.service.GameService;
import org.example.gamestoreapp.service.session.UserHelperService;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class GameServiceImpl implements GameService {
    private final GameRepository gameRepository;
    private final ModelMapper modelMapper;
    private final UserHelperService userHelperService;
    private final UserRepository userRepository;
    private final ApiService steamApiService;

    private final static Logger log = LoggerFactory.getLogger(GameServiceImpl.class);

    public GameServiceImpl(GameRepository gameRepository, ModelMapper modelMapper, UserHelperService userHelperService, UserRepository userRepository, ApiService steamApiService) {
        this.gameRepository = gameRepository;
        this.modelMapper = modelMapper;
        this.userHelperService = userHelperService;
        this.userRepository = userRepository;
        this.steamApiService = steamApiService;
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
    public Optional<GameDTO> getGameById(Long id) {
        Optional<Game> optionalGame = gameRepository.findById(id);

        return optionalGame.map(game -> modelMapper.map(game, GameDTO.class));
    }

    @Override
    public List<GameDTO> getGamesByIds(Set<Long> gameIds) {

        return gameRepository.findByIdIn(gameIds).stream()
                .map(game -> modelMapper.map(game, GameDTO.class))
                .toList();
    }

    @Override
    @Scheduled(cron = "0 10 0 * * *")
    public void updatePricesAt0010() {
        List<Game> gameList = gameRepository.findAll().parallelStream()
                .filter(game -> game.getAppId() != 0)
                .peek(game -> {
                    try {
                        BigDecimal price = steamApiService.fetchPrice(game.getAppId()).block();
                        if (price != null) {
                            game.setPrice(price);
                        }
                    } catch (Exception e) {
                        log.error("Error fetching price for appId {}: {}", game.getAppId(), e.getMessage());
                    }
                })
                .toList();

        gameRepository.saveAll(gameList);
    }
}
