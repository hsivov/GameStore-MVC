package org.example.gamestoreapp.config;

import org.example.gamestoreapp.model.dto.GameDTO;
import org.example.gamestoreapp.model.dto.ShoppingCartDTO;
import org.example.gamestoreapp.model.dto.UpdateGameBindingModel;
import org.example.gamestoreapp.model.entity.Game;
import org.example.gamestoreapp.model.entity.ShoppingCart;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Configuration
public class AppConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();

        // Define a DateTimeFormatter
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

        modelMapper.addConverter((Converter<LocalDateTime, String>) mappingContext ->
                mappingContext.getSource() == null
                        ? null
                        : formatter.format(mappingContext.getSource()));

        modelMapper
                .typeMap(Game.class, GameDTO.class)
                .addMappings(mapper -> mapper
                        .map(src -> src.getGenre().getName(), GameDTO::setGenre)
                );

        modelMapper
                .typeMap(Game.class, UpdateGameBindingModel.class)
                .addMappings(mapper -> mapper
                        .map(src -> src.getGenre().getName(), UpdateGameBindingModel::setGenre)
                );

        modelMapper
                .typeMap(ShoppingCart.class, ShoppingCartDTO.class)
                .addMappings(mapper -> mapper
                        .map(ShoppingCart::getTotalItems, ShoppingCartDTO::setTotalItems))
                .addMappings(mapper -> mapper
                        .map(ShoppingCart::getTotalPrice, ShoppingCartDTO::setTotalPrice));

        return modelMapper;
    }
}
