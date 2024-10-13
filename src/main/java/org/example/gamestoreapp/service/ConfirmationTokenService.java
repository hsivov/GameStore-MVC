package org.example.gamestoreapp.service;

import org.example.gamestoreapp.model.entity.ConfirmationToken;

import java.util.Optional;

public interface ConfirmationTokenService {
    void saveConfirmationToken(ConfirmationToken token);

    Optional<ConfirmationToken> getToken(String token);
}
