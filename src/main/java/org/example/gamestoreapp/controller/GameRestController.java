package org.example.gamestoreapp.controller;

import org.example.gamestoreapp.model.dto.GameDTO;
import org.example.gamestoreapp.service.GameService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/games")
public class GameRestController {
    private final GameService gameService;

    public GameRestController(GameService gameService) {
        this.gameService = gameService;
    }

    @GetMapping("/{gameId}")
    public ResponseEntity<GameDTO> getGameById(@PathVariable("gameId") Long gameId) {
        return gameService.getGameById(gameId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<List<GameDTO>> getGamesByIds(@RequestBody Set<Long> gameIds) {
        List<GameDTO> games = gameService.getGamesByIds(gameIds);

        if (games.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(games);
    }
}
