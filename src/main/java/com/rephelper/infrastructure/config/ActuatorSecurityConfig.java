package com.rephelper.infrastructure.config;

import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class ActuatorSecurityConfig {

    @Bean
    @Order(1) // Ordem mais alta que o SecurityFilterChain padrão
    public SecurityFilterChain actuatorSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/api/v1/actuator/**")
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/api/v1/actuator/health/**").permitAll()
                .requestMatchers("/api/v1/actuator/info").permitAll()
                .requestMatchers("/api/v1/actuator/metrics/**").permitAll()
                .anyRequest().authenticated()
            )
            .csrf(AbstractHttpConfigurer::disable);
        
        return http.build();
    }
} 