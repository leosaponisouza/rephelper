package com.rephelper.infrastructure.adapter.security;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Filtro para autenticação baseada em JWT
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    
    // Lista de caminhos públicos que não requerem autenticação
    private static final List<String> PUBLIC_PATHS = Arrays.asList(
        "/api/v1/auth/**", // Usando wildcard para todos os endpoints de autenticação
        "/auth/login",
        "/api/v1/users",
        "/api/v1/health",
        "/api/v1/system/status",
        "/debug/**",   // Adicionar caminho de debug temporário
        "/api-docs",
        "/swagger-ui",
        "/actuator"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String path = request.getRequestURI();
            String method = request.getMethod();
            
            log.debug("JwtAuthenticationFilter: processando requisição {} {}", method, path);
            
            // Verificar se o caminho está na lista de caminhos públicos
            if (isPublicPath(path)) {
                log.info("JwtAuthenticationFilter: permitindo acesso sem autenticação para: {} {}", method, path);
                filterChain.doFilter(request, response);
                return;
            }
            
            log.info("JwtAuthenticationFilter: Caminho requer autenticação: {} {}", method, path);
            
            // Obter token do cabeçalho
            String jwt = getJwtFromRequest(request);
            
            if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
                // Token válido, autenticar usuário
                Authentication authentication = tokenProvider.getAuthentication(jwt);
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug("Usuário autenticado com sucesso via JWT");
            } else {
                // Token inválido ou não fornecido
                log.warn("Token JWT inválido ou não fornecido para: {} {}", method, path);
                SecurityContextHolder.clearContext();
            }
            
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            log.error("JwtAuthenticationFilter: erro ao processar token JWT", e);
            SecurityContextHolder.clearContext();
            filterChain.doFilter(request, response);
        }
    }
    
    /**
     * Verifica se o caminho está na lista de caminhos públicos
     */
    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }
    
    /**
     * Obtém o token JWT do cabeçalho Authorization
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}

