package org.example.gamestoreapp.config;

import org.example.gamestoreapp.model.dto.GameDTO;
import org.example.gamestoreapp.model.entity.Game;
import org.example.gamestoreapp.model.enums.GenreName;
import org.example.gamestoreapp.util.GenreConverter;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AppConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();

        // Define a Converter for GenreName -> String using GenreConverter
        Converter<GenreName, String> genreConverter = context -> GenreConverter.getGenreDescription(context.getSource());

        // Apply the Converter to the mapping
        modelMapper
                .typeMap(Game.class, GameDTO.class)
                .addMappings(mapper -> mapper
                        .using(genreConverter).map(src -> src.getGenre().getName(), GameDTO::setGenre)
        );

        return modelMapper;
    }
}
