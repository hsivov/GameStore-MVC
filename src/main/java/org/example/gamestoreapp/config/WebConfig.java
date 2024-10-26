package org.example.gamestoreapp.config;

import org.example.gamestoreapp.interceptor.HMACInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final HMACInterceptor hmacInterceptor;

    public WebConfig(HMACInterceptor hmacInterceptor) {
        this.hmacInterceptor = hmacInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(hmacInterceptor)
                .addPathPatterns("/api/**");
    }
}
