package com.rephelper.interfaces.rest.actuator;

import org.springframework.boot.actuate.health.HealthComponent;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/system")
@RequiredArgsConstructor
@Slf4j
public class ActuatorTestController {

    private final HealthEndpoint healthEndpoint;

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        log.info("Tentando acessar endpoint de status");
        Map<String, Object> response = new HashMap<>();
        
        try {
            HealthComponent health = healthEndpoint.health();
            response.put("status", health.getStatus().getCode());
            response.put("health", health);
            response.put("actuatorAvailable", true);
            log.info("Status do sistema obtido com sucesso: {}", health.getStatus().getCode());
        } catch (Exception e) {
            log.error("Erro ao obter status do sistema: {}", e.getMessage(), e);
            response.put("status", "ERROR");
            response.put("message", "Não foi possível obter o status do sistema: " + e.getMessage());
            response.put("actuatorAvailable", false);
        }
        
        return ResponseEntity.ok(response);
    }
} 