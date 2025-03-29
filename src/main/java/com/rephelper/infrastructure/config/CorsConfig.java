package com.rephelper.infrastructure.config;

import java.util.List;
import java.util.Arrays;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Configuration
@ConfigurationProperties(prefix = "cors")
@Getter
@Setter
@Slf4j
public class CorsConfig implements WebMvcConfigurer {

    private List<String> allowedOrigins;
    private List<String> allowedMethods;
    private List<String> allowedHeaders;
    private Long maxAge; // Use Long for maxAge


    @Override
    public void addCorsMappings(CorsRegistry registry) {
        log.info("Configurando CORS com origins: {}", allowedOrigins);
        
        // Se contém "*", não podemos usar allowCredentials=true
        if (allowedOrigins != null && allowedOrigins.contains("*")) {
            log.info("Usando allowedOriginPatterns='*' em vez de allowedOrigins='*'");
            registry.addMapping("/**") // Apply to all endpoints
                    .allowedOriginPatterns("*") // Usar allowedOriginPatterns em vez de allowedOrigins
                    .allowedMethods(allowedMethods != null ? allowedMethods.toArray(new String[0]) : 
                                   new String[]{"GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"})
                    .allowedHeaders(allowedHeaders != null ? allowedHeaders.toArray(new String[0]) : 
                                   new String[]{"Authorization", "Content-Type", "X-Requested-With", "Accept", "Origin"})
                    .allowCredentials(true)
                    .maxAge(maxAge != null ? maxAge : 3600);
        } else {
            registry.addMapping("/**") // Apply to all endpoints
                    .allowedOrigins(allowedOrigins != null ? allowedOrigins.toArray(new String[0]) : 
                                   new String[]{"http://localhost:4200"})
                    .allowedMethods(allowedMethods != null ? allowedMethods.toArray(new String[0]) : 
                                   new String[]{"GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"})
                    .allowedHeaders(allowedHeaders != null ? allowedHeaders.toArray(new String[0]) : 
                                   new String[]{"Authorization", "Content-Type", "X-Requested-With", "Accept", "Origin"})
                    .allowCredentials(true)
                    .maxAge(maxAge != null ? maxAge : 3600);
        }
    }
}