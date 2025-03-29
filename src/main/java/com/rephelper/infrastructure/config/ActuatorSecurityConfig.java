package com.rephelper.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class ActuatorSecurityConfig {

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        log.info("Configurando WebSecurityCustomizer para ignorar endpoints do Actuator");
        return (web) -> web.ignoring()
                .requestMatchers("/api/v1/actuator/**");
    }

    @Bean
    @Order(1)
    public SecurityFilterChain actuatorSecurityFilterChain(HttpSecurity http) throws Exception {
        log.info("Configurando segurança específica para endpoints do Actuator");
        
        http
            .securityMatcher("/api/v1/actuator/**")
            .authorizeHttpRequests(authorize -> authorize
                .anyRequest().permitAll()
            )
            .csrf(AbstractHttpConfigurer::disable);
        
        log.info("Configuração de segurança para Actuator concluída");
        return http.build();
    }
} 