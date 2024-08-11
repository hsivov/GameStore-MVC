package org.example.gamestoreapp.util;

import org.example.gamestoreapp.model.enums.GenreName;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class GenreConverter {
    private static final Map<GenreName, String> genreDescriptions = new HashMap<>();

    static {
        genreDescriptions.put(GenreName.ACTION, "Action");
        genreDescriptions.put(GenreName.ADVENTURE, "Adventure");
        genreDescriptions.put(GenreName.ROLE_PLAYING, "Role-Playing");
        genreDescriptions.put(GenreName.STRATEGY, "Strategy");
        genreDescriptions.put(GenreName.SIMULATION, "Simulation");
        genreDescriptions.put(GenreName.SPORTS_RACING, "Sports and Racing");
    }

    public static String getGenreDescription(GenreName genre) {
        return genreDescriptions.getOrDefault(genre, "Unknown Genre");
    }

    public static Map<GenreName, String> getAllGenreDescriptions() {
        return new HashMap<>(genreDescriptions);
    }
}
