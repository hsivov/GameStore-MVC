package org.example.gamestoreapp.config;

import org.example.gamestoreapp.interceptor.HMACInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final HMACInterceptor hmacInterceptor;

    public WebConfig(HMACInterceptor hmacInterceptor) {
        this.hmacInterceptor = hmacInterceptor;
    }

    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor interceptor = new LocaleChangeInterceptor();
        interceptor.setParamName("lang");
        return interceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry
                .addInterceptor(hmacInterceptor)
                .addPathPatterns("/api/**");

        registry.addInterceptor(localeChangeInterceptor());
    }
}
