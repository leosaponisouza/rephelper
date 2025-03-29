package com.rephelper.infrastructure.adapter.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException; /**
 * Ponto de entrada para erros de autenticação JWT
 */
@Component
@Slf4j
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException)
            throws IOException, ServletException {

        log.error("Erro de autenticação: {}", authException.getMessage());
        log.debug("Requisição não autenticada para: {} {}", request.getMethod(), request.getRequestURI());

        // Configurar resposta de erro
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        
        // Mensagem amigável para o cliente
        String path = request.getRequestURI();
        String message;
        
        if (path.contains("/api/v1/auth")) {
            message = "Falha na autenticação. Verifique suas credenciais e tente novamente.";
        } else {
            message = "Autenticação necessária para acessar este recurso. Por favor, faça login.";
        }
        
        response.getWriter().write("{\"status\":\"error\",\"code\":401,\"message\":\"" + message + "\"}");
    }
}
