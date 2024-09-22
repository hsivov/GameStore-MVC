package org.example.gamestoreapp.service.session;

import org.example.gamestoreapp.model.dto.ShoppingCartDTO;
import org.example.gamestoreapp.model.entity.ShoppingCart;
import org.example.gamestoreapp.model.entity.User;
import org.example.gamestoreapp.repository.ShoppingCartRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Component
@Transactional
public class CartHelperService {
    private final ShoppingCartRepository shoppingCartRepository;
    private final UserHelperService userHelperService;
    private final ModelMapper modelMapper;

    public CartHelperService(ShoppingCartRepository shoppingCartRepository, UserHelperService userHelperService, ModelMapper modelMapper) {
        this.shoppingCartRepository = shoppingCartRepository;
        this.userHelperService = userHelperService;
        this.modelMapper = modelMapper;
    }

    public int getTotalItems() {
        User currentUser = userHelperService.getUser();
        Optional<ShoppingCart> shoppingCartOptional = shoppingCartRepository.findByCustomer(currentUser);

        if (shoppingCartOptional.isPresent()) {
            return modelMapper.map(shoppingCartOptional, ShoppingCartDTO.class).getTotalItems();
        }

        return 0;
    }

    public BigDecimal getTotalPrice() {
        User currentUser = userHelperService.getUser();
        Optional<ShoppingCart> shoppingCartOptional = shoppingCartRepository.findByCustomer(currentUser);

        if (shoppingCartOptional.isPresent()) {
            return modelMapper.map(shoppingCartOptional, ShoppingCartDTO.class).getTotalPrice();
        }

        return BigDecimal.ZERO;
    }
}
