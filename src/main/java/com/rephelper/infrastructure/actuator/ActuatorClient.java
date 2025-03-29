package com.rephelper.infrastructure.actuator;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;

/**
 * Classe utilitária para testar o acesso aos endpoints do Actuator
 * em ambiente de desenvolvimento ou teste
 */
@Configuration
@Slf4j
@Profile("!prod") // Não executar em produção
public class ActuatorClient {

    @Bean
    public CommandLineRunner actuatorHealthTest() {
        return args -> {
            try {
                log.info("Testando acesso ao endpoint de saúde do Actuator...");
                RestTemplate restTemplate = new RestTemplate();
                String baseUrl = "http://localhost:3000"; // Porta de desenvolvimento
                
                String response = restTemplate.getForObject(
                    baseUrl + "/api/v1/actuator/health", 
                    String.class
                );
                
                log.info("Resposta do endpoint de saúde: {}", response);
                log.info("O acesso ao Actuator está funcionando corretamente!");
            } catch (Exception e) {
                log.error("Erro ao acessar o endpoint de saúde do Actuator: {}", e.getMessage());
                log.error("Verifique se a aplicação está rodando e se as configurações de segurança estão corretas");
            }
        };
    }
} 