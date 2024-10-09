package org.example.gamestoreapp.service.impl;

import org.example.gamestoreapp.model.entity.Game;
import org.example.gamestoreapp.model.entity.Order;
import org.example.gamestoreapp.model.entity.ShoppingCart;
import org.example.gamestoreapp.model.entity.User;
import org.example.gamestoreapp.model.enums.OrderStatus;
import org.example.gamestoreapp.repository.OrderRepository;
import org.example.gamestoreapp.repository.ShoppingCartRepository;
import org.example.gamestoreapp.repository.UserRepository;
import org.example.gamestoreapp.service.CheckoutService;
import org.example.gamestoreapp.service.session.CartHelperService;
import org.example.gamestoreapp.service.session.UserHelperService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

@Service
public class CheckoutServiceImpl implements CheckoutService {
    private final UserHelperService userHelperService;
    private final ShoppingCartRepository shoppingCartRepository;
    private final CartHelperService cartHelperService;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    public CheckoutServiceImpl(UserHelperService userHelperService, ShoppingCartRepository shoppingCartRepository, CartHelperService cartHelperService, OrderRepository orderRepository, UserRepository userRepository) {
        this.userHelperService = userHelperService;
        this.shoppingCartRepository = shoppingCartRepository;
        this.cartHelperService = cartHelperService;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public void payment() {
        User currentUser = userHelperService.getUser();
        Optional<ShoppingCart> cart = shoppingCartRepository.findByCustomer(currentUser);

        if (cart.isPresent()) {
            ShoppingCart shoppingCart = cart.get();
            Set<Game> games = shoppingCart.getGames();
            currentUser.getOwnedGames().addAll(games);

            Order order = new Order();
            order.setCustomer(currentUser);
            order.setOrderDate(LocalDateTime.now());
            order.setBoughtGames(games);
            order.setStatus(OrderStatus.APPROVED);
            order.setTotalPrice(cartHelperService.getTotalPrice());

            orderRepository.save(order);
            userRepository.save(currentUser);
            shoppingCartRepository.delete(shoppingCart);
        }
    }
}
