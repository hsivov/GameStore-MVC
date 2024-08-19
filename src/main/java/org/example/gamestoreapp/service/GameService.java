package org.example.gamestoreapp.service;

import org.example.gamestoreapp.model.dto.GameDTO;

import java.util.List;

public interface GameService {
    List<GameDTO> getAll();
}
