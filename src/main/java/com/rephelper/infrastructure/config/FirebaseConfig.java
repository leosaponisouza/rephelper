package com.rephelper.infrastructure.config;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

/**
 * Configuração do Firebase
 */
@Configuration
@Slf4j
public class FirebaseConfig {

    @Value("${rephelper.firebase.service-account-key}")
    private String firebaseCredentials;

    @Value("${rephelper.firebase.config.storage-bucket:}")
    private String storageBucket;

    @Value("${rephelper.firebase.config.project-id:}")
    private String projectId;

    /**
     * Inicializa o Firebase com as credenciais fornecidas
     */
    @PostConstruct
    public void initialize() {
        try {
            if (StringUtils.hasText(firebaseCredentials)) {
                if (FirebaseApp.getApps().isEmpty()) {
                    ByteArrayInputStream credentialsStream =
                            new ByteArrayInputStream(firebaseCredentials.getBytes(StandardCharsets.UTF_8));

                    FirebaseOptions.Builder optionsBuilder = FirebaseOptions.builder()
                            .setCredentials(GoogleCredentials.fromStream(credentialsStream));

                    if (StringUtils.hasText(storageBucket)) {
                        optionsBuilder.setStorageBucket(storageBucket);
                    }

                    if (StringUtils.hasText(projectId)) {
                        optionsBuilder.setProjectId(projectId);
                    }

                    FirebaseOptions options = optionsBuilder.build();
                    FirebaseApp.initializeApp(options);
                    log.info("Firebase has been initialized successfully using provided credentials");
                } else {
                    log.info("Firebase app already initialized");
                }
            } else {
                log.warn("Firebase credentials not provided. Firebase integration disabled.");
            }
        } catch (IOException e) {
            log.error("Error initializing Firebase", e);
            throw new RuntimeException("Could not initialize Firebase", e);
        }
    }

    /**
     * Bean para o FirebaseAuth
     */
    @Bean
    public FirebaseAuth firebaseAuth() {
        return FirebaseAuth.getInstance();
    }
}