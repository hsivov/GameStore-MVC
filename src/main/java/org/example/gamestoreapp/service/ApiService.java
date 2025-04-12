package org.example.gamestoreapp.service;

import reactor.core.publisher.Mono;

import java.math.BigDecimal;

public interface ApiService {
    Mono<BigDecimal> fetchPrice(Integer appId);
}
