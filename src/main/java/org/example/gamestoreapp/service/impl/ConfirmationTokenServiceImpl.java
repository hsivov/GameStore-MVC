package org.example.gamestoreapp.service.impl;

import org.example.gamestoreapp.model.entity.ConfirmationToken;
import org.example.gamestoreapp.repository.ConfirmationTokenRepository;
import org.example.gamestoreapp.service.ConfirmationTokenService;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ConfirmationTokenServiceImpl implements ConfirmationTokenService {

    private final ConfirmationTokenRepository tokenRepository;

    public ConfirmationTokenServiceImpl(ConfirmationTokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    @Override
    public void saveConfirmationToken(ConfirmationToken token) {
        tokenRepository.save(token);
    }

    @Override
    public Optional<ConfirmationToken> getToken(String token) {
        return tokenRepository.findByToken(token);
    }
}

