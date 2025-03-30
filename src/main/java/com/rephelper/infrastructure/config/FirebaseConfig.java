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
    
    private boolean firebaseInitialized = false;

    /**
     * Inicializa o Firebase com as credenciais fornecidas
     */
    @PostConstruct
    public void initialize() {
        try {
            if (StringUtils.hasText(firebaseCredentials)) {
                if (FirebaseApp.getApps().isEmpty()) {
                    log.info("Iniciando inicialização do Firebase com credenciais fornecidas");
                    ByteArrayInputStream credentialsStream =
                            new ByteArrayInputStream(firebaseCredentials.getBytes(StandardCharsets.UTF_8));

                    FirebaseOptions.Builder optionsBuilder = FirebaseOptions.builder()
                            .setCredentials(GoogleCredentials.fromStream(credentialsStream));

                    if (StringUtils.hasText(storageBucket)) {
                        optionsBuilder.setStorageBucket(storageBucket);
                        log.info("Storage bucket configurado: {}", storageBucket);
                    }

                    if (StringUtils.hasText(projectId)) {
                        optionsBuilder.setProjectId(projectId);
                        log.info("Project ID configurado: {}", projectId);
                    }

                    FirebaseOptions options = optionsBuilder.build();
                    FirebaseApp.initializeApp(options);
                    firebaseInitialized = true;
                    log.info("Firebase foi inicializado com sucesso usando credenciais fornecidas");
                } else {
                    firebaseInitialized = true;
                    log.info("Aplicação Firebase já inicializada");
                }
            } else {
                log.warn("Credenciais do Firebase não fornecidas. Integração Firebase desabilitada.");
            }
        } catch (Exception e) {
            log.error("Erro ao inicializar Firebase", e);
            log.warn("A aplicação continuará sem o Firebase");
        }
    }

    /**
     * Bean para o FirebaseAuth
     */
    @Bean
    public FirebaseAuth firebaseAuth() {
        try {
            if (!firebaseInitialized && FirebaseApp.getApps().isEmpty()) {
                log.warn("Firebase não inicializado, tentando criar Firebase App com configuração padrão");
                try {
                    FirebaseOptions options = FirebaseOptions.builder()
                            .setCredentials(GoogleCredentials.getApplicationDefault())
                            .build();
                    FirebaseApp.initializeApp(options);
                    firebaseInitialized = true;
                    log.info("Firebase inicializado com credenciais de aplicação padrão");
                } catch (Exception e) {
                    log.error("Não foi possível inicializar Firebase com credenciais padrão", e);
                }
            }
            
            if (firebaseInitialized || !FirebaseApp.getApps().isEmpty()) {
                return FirebaseAuth.getInstance();
            } else {
                throw new RuntimeException("Firebase não inicializado e não foi possível criar uma instância padrão");
            }
        } catch (Exception e) {
            log.error("Erro ao obter instância do FirebaseAuth", e);
            throw new RuntimeException("Falha ao obter FirebaseAuth", e);
        }
    }
}