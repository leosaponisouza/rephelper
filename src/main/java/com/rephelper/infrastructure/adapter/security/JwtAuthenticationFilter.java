package com.rephelper.infrastructure.adapter.security;

import java.io.IOException;

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

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String path = request.getRequestURI();
            log.debug("JwtAuthenticationFilter: processando requisição para {}", path);
            
            // Verificar se é um endpoint do Actuator
            if (path.startsWith("/api/v1/actuator")) {
                log.debug("JwtAuthenticationFilter: pulando autenticação para endpoint do Actuator: {}", path);
                filterChain.doFilter(request, response);
                return;
            }
            
            String jwt = getJwtFromRequest(request);

            if (StringUtils.hasText(jwt)) {
                // Verificar se é um endpoint de login (que aceita token Firebase)
                if (path.endsWith("/auth/login") || path.endsWith("/auth/logout") || path.endsWith("/users")) {
                    log.debug("JwtAuthenticationFilter: pulando validação JWT para endpoint: {}", path);
                } else {
                    // Para outros endpoints, validar como um JWT normal
                    log.debug("JwtAuthenticationFilter: validando JWT para {}", path);
                    if (tokenProvider.validateToken(jwt)) {
                        Authentication authentication = tokenProvider.getAuthentication(jwt);
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        log.debug("JwtAuthenticationFilter: autenticação bem-sucedida para {}", path);
                    } else {
                        log.warn("JwtAuthenticationFilter: token inválido para {}", path);
                    }
                }
            } else {
                log.debug("JwtAuthenticationFilter: nenhum token JWT encontrado para {}", path);
            }
        } catch (Exception e) {
            log.error("JwtAuthenticationFilter: erro ao processar autenticação: {}", e.getMessage(), e);
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Obtém o token JWT do cabeçalho Authorization da requisição
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}

