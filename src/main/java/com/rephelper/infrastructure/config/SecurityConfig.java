package com.rephelper.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.rephelper.infrastructure.adapter.security.JwtAuthenticationEntryPoint;
import com.rephelper.infrastructure.adapter.security.JwtAuthenticationFilter;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {

    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    
    // Lista de caminhos públicos que não requerem autenticação
    private static final List<String> PUBLIC_PATHS = Arrays.asList(
        "/api/v1/auth/**",
        "/auth/login",
        "/api/v1/users",
        "/api/v1/health/**",
        "/api/v1/system/status",
        "/debug/**", // Endpoint temporário para diagnóstico
        "/api-docs/**",
        "/swagger-ui/**",
        "/swagger-ui.html",
        "/actuator/**"
    );

    @PostConstruct
    public void init() {
        log.info("Inicializando configuração de segurança");
        log.info("Caminhos públicos configurados: {}", PUBLIC_PATHS);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, CorsConfigurationSource corsConfigurationSource) throws Exception {
        log.info("Configurando SecurityFilterChain");
        
        http
            // Desabilitar CSRF para APIs RESTful
            .csrf(AbstractHttpConfigurer::disable)
            
            // Configurar CORS
            .cors(cors -> cors.configurationSource(corsConfigurationSource))
            
            // Configurar tratamento de exceções
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint(jwtAuthenticationEntryPoint)
            )
            
            // Configurar gerenciamento de sessão
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            
            // Configurar regras de autorização
            .authorizeHttpRequests(authorize -> {
                log.info("Configurando regras de autorização HTTP");
                
                // Permitir acesso a caminhos públicos
                PUBLIC_PATHS.forEach(path -> {
                    try {
                        authorize.requestMatchers(path).permitAll();
                        log.info("Permitindo acesso a: {}", path);
                    } catch (Exception e) {
                        log.error("Erro ao configurar caminho público: {}", path, e);
                    }
                });
                
                // Permitir OPTIONS para todos os endpoints (para CORS preflight)
                authorize.requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll();
                
                // Exigir autenticação para todos os outros caminhos
                authorize.anyRequest().authenticated();
                log.info("Configuração de autorização concluída");
            })
            
            // Adicionar filtro JWT antes do filtro de autenticação padrão
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        log.info("Configuração de segurança concluída");
        return http.build();
    }
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource(
            @Value("${cors.allowed-origins:*}") String[] allowedOrigins,
            @Value("${cors.allowed-methods:GET,POST,PUT,DELETE,OPTIONS,PATCH}") String[] allowedMethods,
            @Value("${cors.allowed-headers:Authorization,Content-Type,X-Requested-With,Accept,Origin,x-auth-token}") String[] allowedHeaders,
            @Value("${cors.max-age:3600}") long maxAge) {
        
        log.info("Configurando CORS com origens permitidas: {}", Arrays.toString(allowedOrigins));
        
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(allowedOrigins));
        configuration.setAllowedMethods(Arrays.asList(allowedMethods));
        configuration.setAllowedHeaders(Arrays.asList(allowedHeaders));
        configuration.setExposedHeaders(Arrays.asList("Authorization"));
        configuration.setAllowCredentials(false);
        configuration.setMaxAge(maxAge);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
}