package org.example.gamestoreapp.service.impl;

import jakarta.transaction.Transactional;
import org.example.gamestoreapp.model.dto.GameDTO;
import org.example.gamestoreapp.model.entity.Game;
import org.example.gamestoreapp.model.entity.ShoppingCart;
import org.example.gamestoreapp.model.entity.User;
import org.example.gamestoreapp.repository.GameRepository;
import org.example.gamestoreapp.repository.ShoppingCartRepository;
import org.example.gamestoreapp.service.ShoppingCartService;
import org.example.gamestoreapp.service.session.UserHelperService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ShoppingCartServiceImpl implements ShoppingCartService {

    private final UserHelperService userHelperService;
    private final ShoppingCartRepository shoppingCartRepository;
    private final GameRepository gameRepository;
    private final ModelMapper modelMapper;

    public ShoppingCartServiceImpl(UserHelperService userHelperService, ShoppingCartRepository shoppingCartRepository, GameRepository gameRepository, ModelMapper modelMapper) {
        this.userHelperService = userHelperService;
        this.shoppingCartRepository = shoppingCartRepository;
        this.gameRepository = gameRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    @Transactional
    public void addToCart(Long gameId) {
        User currentUser = userHelperService.getUser();
        ShoppingCart shoppingCart = shoppingCartRepository.findByCustomer(currentUser)
                .orElse(new ShoppingCart());

        Optional<Game> game = gameRepository.findById(gameId);

        Set<Game> games = shoppingCart.getGames();

        game.ifPresent(games::add);

        shoppingCart.setCustomer(currentUser);

        shoppingCartRepository.save(shoppingCart);
    }

    @Override
    @Transactional
    public Set<GameDTO> getAddedGames() {
        User currentUser = userHelperService.getUser();

        Set<Game> games = shoppingCartRepository.findByCustomer(currentUser)
                .map(ShoppingCart::getGames).orElse(Collections.emptySet());

        return games.stream()
                .map(game -> modelMapper.map(game, GameDTO.class))
                .collect(Collectors.toSet());
    }
}
