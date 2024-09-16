package org.example.gamestoreapp.init;

import org.example.gamestoreapp.model.entity.Genre;
import org.example.gamestoreapp.model.enums.GenreName;
import org.example.gamestoreapp.repository.GenreRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class GenreInit implements CommandLineRunner {

    private final GenreRepository genreRepository;

    public GenreInit(GenreRepository genreRepository) {
        this.genreRepository = genreRepository;
    }

    @Override
    public void run(String... args) {
        long count = genreRepository.count();

        if (count == 0) {
            List<Genre> genres = new ArrayList<>();

            Arrays.stream(GenreName.values())
                    .forEach(name -> {
                        Genre genre = new Genre();
                        genre.setName(name);
                        genres.add(genre);
                    });

            genreRepository.saveAll(genres);
        }
    }
}
