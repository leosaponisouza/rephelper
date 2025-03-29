package com.rephelper.interfaces.rest.health;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/health")
@Tag(name = "Health", description = "Health check endpoints")
public class HealthController {

    @GetMapping
    @Operation(summary = "Check API health", description = "Simple health check endpoint")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("message", "Service is running");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/details")
    @Operation(summary = "Detailed health check", description = "Returns detailed health information")
    public ResponseEntity<Map<String, Object>> detailedHealthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", LocalDateTime.now().toString());

        // Add system info
        Map<String, Object> system = new HashMap<>();
        system.put("java.version", System.getProperty("java.version"));
        system.put("java.vendor", System.getProperty("java.vendor"));
        system.put("os.name", System.getProperty("os.name"));
        system.put("os.version", System.getProperty("os.version"));
        system.put("os.arch", System.getProperty("os.arch"));
        system.put("available.processors", Runtime.getRuntime().availableProcessors());
        system.put("free.memory", Runtime.getRuntime().freeMemory());
        system.put("total.memory", Runtime.getRuntime().totalMemory());
        system.put("max.memory", Runtime.getRuntime().maxMemory());

        response.put("system", system);

        // Add active profiles
        String activeProfiles = System.getProperty("spring.profiles.active", "default");
        response.put("activeProfiles", activeProfiles);

        return ResponseEntity.ok(response);
    }
}