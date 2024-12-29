package org.example.gamestoreapp.init;

import org.example.gamestoreapp.model.entity.Genre;
import org.example.gamestoreapp.repository.GenreRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class GenreInit implements CommandLineRunner {

    private final static Map<String, String> GENRES = Map.ofEntries(
            Map.entry("Action", "The player overcomes challenges by physical means such as precise aim and quick response times."),
            Map.entry("Adventure", "the player assumes the role of a protagonist in an interactive story, driven by exploration and/or puzzle-solving."),
            Map.entry("Role Playing", "Role-playing games (or RPGs) are video games where players engage with " +
                    "the gameworld through characters who have backstories and existing motivations. " +
                    "The RPG genre often includes NPCs (non-player characters)," +
                    " side quests, downloadable content (dlc), and larger story arcs."),
            Map.entry("Simulation", "Games that are designed to mimic activities you'd see in the real world. " +
                    "The purpose of the game may be to teach you something. For example, you could learn how to fish. " +
                    "Others simulation games take on operating a business such as a farm or a theme park."),
            Map.entry("Sports & Racing", "A subgenre within simulations focused on the practice of traditional sports, " +
                    "including team sports, athletics, and extreme sports."),
            Map.entry("Strategy", "Players succeed (or lose) based on strategic decisions, not luck. " +
                    "Players have equal knowledge to play; no trivia. Play is based on multiple decisions a person could" +
                    " make on each turn with possible advantages and disadvantages each time."),
            Map.entry("MMO", "MMO (Massively Multiplayer Online) games are a genre where large numbers of players interact with one " +
                    "another in a virtual world. These games typically feature expansive, persistent online environments that continue to evolve " +
                    "even when the player is not active. Players can collaborate, compete, and form social structures such as guilds or alliances, " +
                    "contributing to a sense of community.")
    );

    private final GenreRepository genreRepository;

    private final static Logger LOGGER = LoggerFactory.getLogger(GenreInit.class);

    public GenreInit(GenreRepository genreRepository) {
        this.genreRepository = genreRepository;
    }

    @Override
    public void run(String... args) {
        LOGGER.info("Initializing genres...");
        long count = genreRepository.count();

        if (count == 0) {
            LOGGER.info("No genres were found. Setting up default genres.");
            List<Genre> genres = new ArrayList<>();
            GENRES.forEach((key, value) -> {
                try {
                    Genre genre = new Genre();
                    genre.setName(key);
                    genre.setDescription(value);
                    genres.add(genre);
                    LOGGER.info("Added genre: {}", key);
                } catch (Exception e) {
                    LOGGER.error("Failed to add genre: {}", key, e);
                }
            });
            genreRepository.saveAll(genres);
        } else {
            LOGGER.info("Genres found. Skipping setup.");
        }
        LOGGER.info("Genre initializing complete.");
    }
}
