package com.rephelper.infrastructure.adapter.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException; /**
 * Ponto de entrada para erros de autenticação JWT
 */
@Component
@Slf4j
public class JwtAuthenticationEntryPoint implements org.springframework.security.web.AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         org.springframework.security.core.AuthenticationException authException)
            throws IOException, ServletException {

        log.error("Unauthorized error: {}", authException.getMessage());
        log.debug("Requisição não autenticada para: {} {}", request.getMethod(), request.getRequestURI());

        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        
        // Mensagem amigável para o cliente
        String mensagem = "Autenticação necessária para acessar este recurso. Por favor, faça login novamente.";
        
        response.getWriter().write("{\"status\":\"error\",\"code\":401,\"message\":\"" + mensagem + "\"}");
    }
}
