package org.example.gamestoreapp.service.impl;

import jakarta.transaction.Transactional;
import org.example.gamestoreapp.model.entity.Game;
import org.example.gamestoreapp.model.entity.ShoppingCart;
import org.example.gamestoreapp.model.entity.User;
import org.example.gamestoreapp.repository.GameRepository;
import org.example.gamestoreapp.repository.ShoppingCartRepository;
import org.example.gamestoreapp.service.ShoppingCartService;
import org.example.gamestoreapp.service.session.UserHelperService;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
public class ShoppingCartServiceImpl implements ShoppingCartService {

    private final UserHelperService userHelperService;
    private final ShoppingCartRepository shoppingCartRepository;
    private final GameRepository gameRepository;

    public ShoppingCartServiceImpl(UserHelperService userHelperService, ShoppingCartRepository shoppingCartRepository, GameRepository gameRepository) {
        this.userHelperService = userHelperService;
        this.shoppingCartRepository = shoppingCartRepository;
        this.gameRepository = gameRepository;
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
}
