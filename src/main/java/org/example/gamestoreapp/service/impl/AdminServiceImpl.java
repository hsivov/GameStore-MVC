package org.example.gamestoreapp.service.impl;

import org.example.gamestoreapp.model.dto.AddGameBindingModel;
import org.example.gamestoreapp.model.entity.Game;
import org.example.gamestoreapp.repository.GameRepository;
import org.example.gamestoreapp.service.AdminService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
public class AdminServiceImpl implements AdminService {
    private final ModelMapper modelMapper;
    private final GameRepository gameRepository;

    public AdminServiceImpl(ModelMapper modelMapper, GameRepository gameRepository) {
        this.modelMapper = modelMapper;
        this.gameRepository = gameRepository;
    }

    @Override
    public void addGame(AddGameBindingModel addGameBindingModel) {
        Game game = modelMapper.map(addGameBindingModel, Game.class);

        gameRepository.save(game);
    }
}
