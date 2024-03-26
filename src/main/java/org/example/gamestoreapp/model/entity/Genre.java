package org.example.gamestoreapp.model.entity;

import jakarta.persistence.*;
import org.example.gamestoreapp.model.enums.GenreName;
@Entity
@Table(name = "genres")
public class Genre extends BaseEntity{
    @Enumerated(EnumType.STRING)
    @Column(nullable = false,unique = true)
    private GenreName name;
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    public GenreName getName() {
        return name;
    }

    public void setName(GenreName name) {
        this.name = name;
        setDescription(name);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    private void setDescription(GenreName name) {
        String description = "";

        switch (name) {
            case ACTION -> description = "The player overcomes challenges by physical means such as precise aim and quick response times.";
            case STRATEGY -> description = "Players succeed (or lose) based on strategic decisions, not luck. " +
                    "Players have equal knowledge to play; no trivia. Play is based on multiple decisions a person could" +
                    " make on each turn with possible advantages and disadvantages each time.";
            case ADVENTURE -> description = "the player assumes the role of a protagonist in an interactive story, driven by exploration and/or puzzle-solving. ";
            case SIMULATION -> description = "Games that are designed to mimic activities you'd see in the real world. " +
                    "The purpose of the game may be to teach you something. For example, you could learn how to fish. " +
                    "Others simulation games take on operating a business such as a farm or a theme park.";
            case ROLE_PLAYING -> description = "Role-playing games (or RPGs) are video games where players engage with " +
                    "the gameworld through characters who have backstories and existing motivations. " +
                    "The RPG genre often includes NPCs (non-player characters)," +
                    " side quests, downloadable content (dlc), and larger story arcs.";
            case SPORTS_RACING -> description = "A subgenre within simulations focused on the practice of traditional sports, " +
                    "including team sports, athletics, and extreme sports.";
        }

        this.description = description;
    }
}
