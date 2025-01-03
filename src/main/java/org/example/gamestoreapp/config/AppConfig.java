package org.example.gamestoreapp.config;

import org.example.gamestoreapp.model.dto.GameDTO;
import org.example.gamestoreapp.model.dto.UpdateGameBindingModel;
import org.example.gamestoreapp.model.entity.Game;
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
                formatter.format(mappingContext.getSource()));

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

        return modelMapper;
    }
}
