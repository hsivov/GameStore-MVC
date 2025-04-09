package org.example.gamestoreapp.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;

@Component
public class OrderServiceClient {
    private final RestTemplate restTemplate;

    @Value("${order.service.url}")
    private String orderServiceUrl;

    @Value("${app.api.key}")
    private String apiKey;

    @Value("${app.api.secret}")
    private String secret;

    public OrderServiceClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public <T> T get(String endpoint, String method, Class<T> responseType) {

        String url = orderServiceUrl + endpoint;

        HttpHeaders headers = setHeaders(method, endpoint);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<T> response = restTemplate.exchange(url, HttpMethod.GET, entity, responseType);

        return response.getBody();
    }

    public <T, R> R post(String endpoint, T requestBody, Class<R> responseType) {
        String url = orderServiceUrl + endpoint;
        String method = "POST";

        HttpHeaders headers = setHeaders(method, endpoint);
        HttpEntity<T> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<R> response = restTemplate.postForEntity(url, entity, responseType);

        return response.getBody();
    }

    private HttpHeaders setHeaders(String method, String endpoint) {
        HttpHeaders headers = new HttpHeaders();

        String timestamp = String.valueOf(Instant.now().getEpochSecond());

        // Construct the payload
        String payload = method + endpoint + timestamp;

        // Generate HMAC signature
        String signature = HMACUtil.generateHMAC(payload, secret);

        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Api-Key", apiKey);
        headers.set("X-Signature", signature);
        headers.set("X-Timestamp", timestamp);

        return headers;
    }
}
