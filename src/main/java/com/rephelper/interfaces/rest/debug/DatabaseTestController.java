package com.rephelper.interfaces.rest.debug;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rephelper.application.dto.response.ApiResponse;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;

@RestController
@RequestMapping("/debug/database")
@Slf4j
public class DatabaseTestController {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Autowired
    private DataSource dataSource;
    
    @Value("${spring.datasource.url}")
    private String dbUrl;
    
    @Value("${spring.datasource.username}")
    private String dbUsername;
    
    @GetMapping("/test")
    public ResponseEntity<ApiResponse> testDatabaseConnection() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Testar conexão simples
            Integer result = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            response.put("connectionTest", result == 1 ? "SUCCESS" : "FAILED");
            
            // Obter informações da conexão
            try (Connection conn = dataSource.getConnection()) {
                DatabaseMetaData metaData = conn.getMetaData();
                
                Map<String, Object> dbInfo = new HashMap<>();
                dbInfo.put("databaseProductName", metaData.getDatabaseProductName());
                dbInfo.put("databaseProductVersion", metaData.getDatabaseProductVersion());
                dbInfo.put("driverName", metaData.getDriverName());
                dbInfo.put("driverVersion", metaData.getDriverVersion());
                dbInfo.put("url", metaData.getURL());
                dbInfo.put("username", metaData.getUserName());
                dbInfo.put("maxConnections", metaData.getMaxConnections());
                
                response.put("databaseInfo", dbInfo);
            }
            
            // Informações de configuração
            Map<String, Object> configInfo = new HashMap<>();
            configInfo.put("jdbcUrl", dbUrl);
            configInfo.put("username", dbUsername);
            
            response.put("configInfo", configInfo);
            
            log.info("Teste de conexão com banco de dados bem-sucedido");
            return ResponseEntity.ok(ApiResponse.builder()
                    .status("SUCCESS")
                    .message("Conexão com banco de dados testada com sucesso")
                    .data(response)
                    .build());
            
        } catch (Exception e) {
            log.error("Erro ao testar conexão com banco de dados: {}", e.getMessage(), e);
            
            response.put("error", e.getMessage());
            response.put("errorType", e.getClass().getName());
            response.put("stackTrace", e.getStackTrace());
            
            return ResponseEntity.ok(ApiResponse.builder()
                    .status("ERROR")
                    .message("Falha ao testar conexão com banco de dados")
                    .data(response)
                    .build());
        }
    }
    
    @GetMapping("/env")
    public ResponseEntity<ApiResponse> getDatabaseEnvironment() {
        Map<String, Object> envVars = new HashMap<>();
        
        // Listar variáveis de ambiente relacionadas ao banco de dados
        envVars.put("DB_HOST", System.getenv("DB_HOST"));
        envVars.put("DB_PORT", System.getenv("DB_PORT"));
        envVars.put("DB_NAME", System.getenv("DB_NAME"));
        envVars.put("DB_USER", System.getenv("DB_USER"));
        envVars.put("JDBC_DATABASE_URL", System.getenv("JDBC_DATABASE_URL"));
        
        log.info("Variáveis de ambiente relacionadas ao banco de dados: {}", envVars);
        
        return ResponseEntity.ok(ApiResponse.builder()
                .status("SUCCESS")
                .message("Variáveis de ambiente relacionadas ao banco de dados")
                .data(envVars)
                .build());
    }
} 