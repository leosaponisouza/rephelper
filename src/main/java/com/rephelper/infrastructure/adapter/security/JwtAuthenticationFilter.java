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
            String method = request.getMethod();
            log.debug("JwtAuthenticationFilter: processando requisição {} {}", method, path);
            
            // Verificar se é um endpoint do Actuator ou permitidos sem autenticação
            if (isPermittedWithoutAuth(path)) {
                log.info("JwtAuthenticationFilter: permitindo acesso sem autenticação para: {} {}", method, path);
                filterChain.doFilter(request, response);
                return;
            }
            
            log.info("JwtAuthenticationFilter: autenticação requerida para: {} {}", method, path);
            
            String jwt = getJwtFromRequest(request);

            if (StringUtils.hasText(jwt)) {
                log.debug("JwtAuthenticationFilter: validando JWT para {} {}", method, path);
                if (tokenProvider.validateToken(jwt)) {
                    Authentication authentication = tokenProvider.getAuthentication(jwt);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    log.debug("JwtAuthenticationFilter: autenticação bem-sucedida para {} {}", method, path);
                } else {
                    log.warn("JwtAuthenticationFilter: token inválido ou expirado para {} {}", method, path);
                    SecurityContextHolder.clearContext();
                }
            } else {
                log.warn("JwtAuthenticationFilter: nenhum token JWT encontrado para {} {} - Autenticação necessária", method, path);
                // Não limpar o contexto aqui, deixe o fluxo continuar e o EntryPoint lidar com isso
            }
        } catch (Exception e) {
            log.error("JwtAuthenticationFilter: erro ao processar autenticação: {}", e.getMessage(), e);
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Verifica se o caminho pode ser acessado sem autenticação
     */
    private boolean isPermittedWithoutAuth(String path) {
        // Opção específica para depuração - permitir todas as rotas de autenticação com qualquer path
        if (path.contains("/auth/login") || path.contains("/login") || 
            path.contains("/auth/signup") || path.contains("/signup") ||
            path.contains("/auth/refresh") || path.contains("/refresh")) {
            log.info("Permitindo acesso para rota de autenticação: {}", path);
            return true;
        }
        
        boolean permitido = path.startsWith("/api/v1/actuator") || 
               path.startsWith("/api/v1/auth") || 
               path.equals("/api/v1/users") ||
               path.startsWith("/api/v1/health") ||
               path.equals("/api/v1/system/status") ||
               path.startsWith("/api-docs") ||
               path.startsWith("/swagger-ui") ||
               path.equals("/swagger-ui.html");
        
        log.info("Rota {} está {}permitida sem autenticação", path, permitido ? "" : "NÃO ");
        return permitido;
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

