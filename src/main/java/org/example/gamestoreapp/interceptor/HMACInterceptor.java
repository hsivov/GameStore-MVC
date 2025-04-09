package org.example.gamestoreapp.interceptor;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.gamestoreapp.util.HMACUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.HashMap;
import java.util.Map;

@Component
public class HMACInterceptor implements HandlerInterceptor {

    @Value("${app.api.key}")
    private String apiKey;

    @Value("${app.api.secret}")
    private String secret;

    private Map<String, String> clientKeys;

    @PostConstruct
    public void init() {
        clientKeys = new HashMap<>();
        clientKeys.put(apiKey, secret);
    }

    @Override
    public boolean preHandle(HttpServletRequest request,
                             @NonNull HttpServletResponse response,
                             @NonNull Object handler) {

        String apiKey = request.getHeader("X-Api-Key");
        String providedSignature = request.getHeader("X-Signature");
        String timestamp = request.getHeader("X-Timestamp");

        // Validate headers are present
        if (apiKey == null || providedSignature == null || timestamp == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

        // Retrieve the secret key for the given API key
        String secret = clientKeys.get(apiKey);
        if (secret == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

        // Create the expected HMAC signature
        String payload = request.getMethod() + request.getRequestURI() + timestamp;
        String expectedSignature = HMACUtil.generateHMAC(payload, secret);

        // Compare the provided signature with the expected signature
        if (!providedSignature.equals(expectedSignature)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return false;
        }

        return true; // Allow the request if signatures match
    }
}
