package com.rephelper.interfaces.rest.debug;

import java.util.HashMap;
import java.util.Map;
import java.util.Enumeration;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rephelper.application.dto.response.ApiResponse;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * Controlador para diagnóstico de problemas em produção
 * Este controlador deve ser removido após a resolução do problema
 */
@RestController
@RequestMapping("/debug")
@Slf4j
public class DebugController {

    @GetMapping("/info")
    public ResponseEntity<ApiResponse> getDebugInfo(HttpServletRequest request) {
        Map<String, Object> info = collectRequestInfo(request);
        
        log.info("Debug info coletada: {}", info);
        
        return ResponseEntity.ok(ApiResponse.builder()
                .status("SUCCESS")
                .message("Debug info coletada com sucesso")
                .data(info)
                .build());
    }
    
    @PostMapping("/echo")
    public ResponseEntity<ApiResponse> echo(@RequestBody(required = false) Object body, HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        response.put("body", body);
        response.put("requestInfo", collectRequestInfo(request));
        
        log.info("Echo recebido: {}", body);
        
        return ResponseEntity.ok(ApiResponse.builder()
                .status("SUCCESS")
                .message("Echo recebido com sucesso")
                .data(response)
                .build());
    }
    
    @GetMapping("/auth/login/test")
    public ResponseEntity<ApiResponse> testAuthLoginEndpoint(HttpServletRequest request) {
        Map<String, Object> info = collectRequestInfo(request);
        
        log.info("Teste do endpoint /auth/login: {}", info);
        
        return ResponseEntity.ok(ApiResponse.builder()
                .status("SUCCESS")
                .message("O endpoint de teste /auth/login está funcionando")
                .data(info)
                .build());
    }
    
    @PostMapping("/auth/login/test")
    public ResponseEntity<ApiResponse> testAuthLoginPostEndpoint(@RequestBody(required = false) Object body, HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        response.put("body", body);
        response.put("requestInfo", collectRequestInfo(request));
        
        log.info("Teste POST do endpoint /auth/login: {}", body);
        
        return ResponseEntity.ok(ApiResponse.builder()
                .status("SUCCESS")
                .message("O endpoint de teste POST /auth/login está funcionando")
                .data(response)
                .build());
    }
    
    @PostMapping("/railway/auth")
    public ResponseEntity<ApiResponse> railwayAuthTest(@RequestBody(required = false) Object body, HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        response.put("body", body);
        response.put("requestInfo", collectRequestInfo(request));
        response.put("deployEnvironment", System.getenv("RAILWAY_ENVIRONMENT"));
        response.put("railwayInfo", collectRailwayEnvInfo());
        
        log.info("Railway auth test: {}", response);
        
        return ResponseEntity.ok(ApiResponse.builder()
                .status("SUCCESS")
                .message("Railway auth test recebido com sucesso")
                .data(response)
                .build());
    }
    
    private Map<String, Object> collectRequestInfo(HttpServletRequest request) {
        Map<String, Object> info = new HashMap<>();
        
        // Informações básicas da requisição
        info.put("method", request.getMethod());
        info.put("requestURI", request.getRequestURI());
        info.put("queryString", request.getQueryString());
        info.put("remoteAddr", request.getRemoteAddr());
        info.put("contextPath", request.getContextPath());
        info.put("servletPath", request.getServletPath());
        
        // Cabeçalhos
        Map<String, String> headers = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();
            headers.put(name, request.getHeader(name));
        }
        info.put("headers", headers);
        
        // Informações do servidor
        Map<String, String> serverInfo = new HashMap<>();
        serverInfo.put("serverName", request.getServerName());
        serverInfo.put("serverPort", String.valueOf(request.getServerPort()));
        serverInfo.put("serverProtocol", request.getProtocol());
        info.put("serverInfo", serverInfo);
        
        return info;
    }
    
    private Map<String, String> collectRailwayEnvInfo() {
        Map<String, String> info = new HashMap<>();
        
        // Informações específicas do Railway
        info.put("RAILWAY_ENVIRONMENT", System.getenv("RAILWAY_ENVIRONMENT"));
        info.put("RAILWAY_SERVICE_NAME", System.getenv("RAILWAY_SERVICE_NAME"));
        info.put("PORT", System.getenv("PORT"));
        info.put("RAILWAY_PROJECT_ID", System.getenv("RAILWAY_PROJECT_ID"));
        info.put("RAILWAY_SERVICE_ID", System.getenv("RAILWAY_SERVICE_ID"));
        
        return info;
    }
} 