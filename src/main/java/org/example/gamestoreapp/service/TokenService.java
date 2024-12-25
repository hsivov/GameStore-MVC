package org.example.gamestoreapp.service;

import org.example.gamestoreapp.model.entity.ConfirmationToken;

import java.util.Optional;

public interface TokenService {
    void saveConfirmationToken(ConfirmationToken token);

    Optional<ConfirmationToken> getToken(String token);

    void verifyToken(String token);

    void invalidateToken(ConfirmationToken token);
}
