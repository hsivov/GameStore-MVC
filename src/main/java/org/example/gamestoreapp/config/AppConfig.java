package org.example.gamestoreapp.config;

import org.example.gamestoreapp.model.dto.GameDTO;
import org.example.gamestoreapp.model.dto.OrderDTO;
import org.example.gamestoreapp.model.entity.Game;
import org.example.gamestoreapp.model.entity.Order;
import org.example.gamestoreapp.model.enums.GenreName;
import org.example.gamestoreapp.util.GenreConverter;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Configuration
public class AppConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();

        // Define a DateTimeFormatter
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

        // Define a Converter for GenreName -> String using GenreConverter
        Converter<GenreName, String> genreConverter = context -> GenreConverter.getGenreDescription(context.getSource());

        Converter<LocalDateTime, String> dateTimeConverter = context -> {
            LocalDateTime dateTime = context.getSource();
            return dateTime.format(formatter);
        };

        // Apply the Converter to the mapping
        modelMapper
                .typeMap(Game.class, GameDTO.class)
                .addMappings(mapper -> mapper
                        .using(genreConverter).map(src -> src.getGenre().getName(), GameDTO::setGenre)
        );

        modelMapper
                .typeMap(Order.class, OrderDTO.class)
                .addMappings(mapper -> mapper
                        .using(dateTimeConverter)
                        .map(Order::getOrderDate, OrderDTO::setOrderDate)
                );

        return modelMapper;
    }
}
