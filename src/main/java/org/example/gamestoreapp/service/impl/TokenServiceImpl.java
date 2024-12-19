package org.example.gamestoreapp.service.impl;

import org.example.gamestoreapp.exception.UsedTokenException;
import org.example.gamestoreapp.exception.TokenExpiredException;
import org.example.gamestoreapp.model.entity.ConfirmationToken;
import org.example.gamestoreapp.repository.ConfirmationTokenRepository;
import org.example.gamestoreapp.service.TokenService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class TokenServiceImpl implements TokenService {

    private final ConfirmationTokenRepository tokenRepository;

    public TokenServiceImpl(ConfirmationTokenRepository tokenRepository) {
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

    @Override
    public void validateToken(String token) {
        ConfirmationToken confirmationToken = getToken(token)
                .orElseThrow(() -> new IllegalStateException("Token not found"));

        if (confirmationToken.getConfirmedAt() != null) {
            throw new UsedTokenException("Account already confirmed");
        }

        LocalDateTime expiredAt = confirmationToken.getExpiresAt();

        if (expiredAt.isBefore(LocalDateTime.now())) {
            throw new TokenExpiredException("Token expired");
        }
    }

    @Override
    public void invalidateToken(ConfirmationToken token) {
        token.setConfirmedAt(LocalDateTime.now());
        tokenRepository.save(token);
    }
}

