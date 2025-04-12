package org.example.gamestoreapp.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.gamestoreapp.service.ApiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@Service
public class SteamApiService implements ApiService {

    private final WebClient.Builder webClientBuilder;

    private final static Logger log = LoggerFactory.getLogger(SteamApiService.class);

    public SteamApiService(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    @Override
    public Mono<BigDecimal> fetchPrice(Integer appId) {
        String apiUrl = "https://store.steampowered.com/api/appdetails?appids=" + appId + "&filters=price_overview";

        return webClientBuilder.baseUrl(apiUrl)
                .build()
                .get()
                .retrieve()
                .bodyToMono(String.class) // Returns a Mono (reactive type) containing the response
                .mapNotNull(this::extractFinalPrice)
                .map(price -> {
                    double priceInBgn = (Double.parseDouble(price) / 100) * 2; // convert price to Bulgarian lev
                    return BigDecimal.valueOf(priceInBgn);
                })
                .doOnError(error -> log.error("Error fetching price for appId {}: {}", appId, error.getMessage()));
    }

    private String extractFinalPrice(String responseBody) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode root = objectMapper.readTree(responseBody);

            // Get the first (and only) field from the root object
            String firstKey = root.fieldNames().next();  // e.g., "2967990"
            JsonNode appNode = root.path(firstKey);

            if (appNode.path("success").asBoolean(false)) {
                return appNode.path("data")
                        .path("price_overview")
                        .path("final")
                        .asText();  // Returns "3499" as string
            }
        } catch (Exception e) {
            log.error("JSON parsing error: {}", e.getMessage());
        }
        return null;
    }
}
