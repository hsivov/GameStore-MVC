package org.example.gamestoreapp.service.impl;

import jakarta.transaction.Transactional;
import org.example.gamestoreapp.model.dto.ShoppingCartDTO;
import org.example.gamestoreapp.model.entity.Game;
import org.example.gamestoreapp.model.entity.ShoppingCart;
import org.example.gamestoreapp.model.entity.User;
import org.example.gamestoreapp.repository.GameRepository;
import org.example.gamestoreapp.repository.ShoppingCartRepository;
import org.example.gamestoreapp.service.ShoppingCartService;
import org.example.gamestoreapp.service.session.UserHelperService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

        List<Game> games = shoppingCart.getGames();

        if (games == null) {
            games = new ArrayList<>();
            shoppingCart.setGames(games);
        }

        game.ifPresent(games::add);

        shoppingCart.setCustomer(currentUser);

        shoppingCartRepository.save(shoppingCart);
    }

    @Override
    @Transactional
    public ShoppingCartDTO getShoppingCart() {
        User currentUser = userHelperService.getUser();

        ShoppingCart shoppingCart = shoppingCartRepository.findByCustomer(currentUser)
                .orElse(new ShoppingCart());

        return modelMapper.map(shoppingCart, ShoppingCartDTO.class);
    }

    @Override
    @Transactional
    public void removeItem(Long gameId) {
        User currentUser = userHelperService.getUser();
        ShoppingCart shoppingCart = shoppingCartRepository.findByCustomer(currentUser)
                .orElseThrow(() -> new RuntimeException("Shopping cart not found"));

        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new RuntimeException("Game not found"));

        // Remove the game from the cart
        if (shoppingCart.getGames().contains(game)) {
            shoppingCart.getGames().remove(game);

            // Save the updated shopping cart
            shoppingCartRepository.save(shoppingCart);
        } else {
            throw new RuntimeException("Game not found in the cart");
        }
    }

    @Override
    @Transactional
    public void removeAll() {
        User currentUser = userHelperService.getUser();
        ShoppingCart shoppingCart = shoppingCartRepository.findByCustomer(currentUser)
                .orElseThrow(() -> new RuntimeException("Shopping cart not found"));

        shoppingCart.getGames().clear();
        shoppingCartRepository.save(shoppingCart);
    }

    @Override
    @Transactional
    public boolean isGameInCart(Long id) {
        User currentUser = userHelperService.getUser();
        Optional<ShoppingCart> byCustomer = shoppingCartRepository.findByCustomer(currentUser);

        Optional<Game> game = gameRepository.findById(id);

        if (byCustomer.isEmpty() || game.isEmpty()) {
            return false;
        }

        return byCustomer.get().getGames().contains(game.get());
    }
}
