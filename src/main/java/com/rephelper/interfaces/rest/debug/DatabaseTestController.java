package com.rephelper.interfaces.rest.debug;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.core.env.Environment;

import com.rephelper.application.dto.response.ApiResponse;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.util.List;

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
    
    @Autowired
    private Environment env;
    
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
    public ResponseEntity<ApiResponse> getEnvironmentVariables() {
        Map<String, String> envVars = new HashMap<>();
        
        // Capturar todas as variáveis de ambiente relacionadas ao banco de dados
        envVars.put("MYSQLHOST", System.getenv("MYSQLHOST"));
        envVars.put("MYSQLPORT", System.getenv("MYSQLPORT"));
        envVars.put("MYSQLDATABASE", System.getenv("MYSQLDATABASE"));
        envVars.put("MYSQLUSER", System.getenv("MYSQLUSER"));
        
        // Não mostrar a senha completa por segurança
        String password = System.getenv("MYSQLPASSWORD");
        if (password != null && !password.isEmpty()) {
            envVars.put("MYSQLPASSWORD", "******");
        } else {
            envVars.put("MYSQLPASSWORD", null);
        }
        
        // Variáveis adicionais do MySQL
        envVars.put("MYSQL_URL", System.getenv("MYSQL_URL"));
        envVars.put("MYSQL_PUBLIC_URL", System.getenv("MYSQL_PUBLIC_URL"));
        
        // Capturar variáveis de ambiente do Spring
        envVars.put("SPRING_PROFILES_ACTIVE", System.getenv("SPRING_PROFILES_ACTIVE"));
        envVars.put("SERVER_PORT", System.getenv("SERVER_PORT"));
        envVars.put("PORT", System.getenv("PORT"));
        
        // Usar ConfigurableEnvironment para obter as propriedades resolvidas
        envVars.put("spring.datasource.url", env.getProperty("spring.datasource.url"));
        envVars.put("spring.datasource.username", env.getProperty("spring.datasource.username"));
        envVars.put("spring.datasource.driver-class-name", env.getProperty("spring.datasource.driver-class-name"));
        
        return ResponseEntity.ok(ApiResponse.builder()
                .status("SUCCESS")
                .message("Variáveis de ambiente")
                .data(envVars)
                .build());
    }

    @GetMapping("/mysql-info")
    public ResponseEntity<ApiResponse> getMySQLInfo() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Verificar tipo de banco (MySQL ou MariaDB)
            String version = jdbcTemplate.queryForObject("SELECT version()", String.class);
            boolean isMariaDB = version != null && version.toLowerCase().contains("mariadb");
            String dbType = isMariaDB ? "MariaDB" : "MySQL";
            
            response.put("databaseType", dbType);
            response.put("fullVersion", version);
            
            // Consultar variáveis do servidor 
            List<Map<String, Object>> variables = jdbcTemplate.queryForList(
                "SHOW VARIABLES LIKE '%timeout%'");
            response.put("timeoutSettings", variables);
            
            // Consultar o status do servidor
            List<Map<String, Object>> status = jdbcTemplate.queryForList(
                "SHOW STATUS LIKE '%conn%'");
            response.put("connectionStatus", status);
            
            // Consultar os bancos de dados disponíveis
            List<String> databases = jdbcTemplate.queryForList("SHOW DATABASES", String.class);
            response.put("availableDatabases", databases);
            
            // Informações específicas do MariaDB (se aplicável)
            if (isMariaDB) {
                try {
                    // Verificar plugins do MariaDB
                    List<Map<String, Object>> plugins = jdbcTemplate.queryForList(
                        "SELECT * FROM information_schema.plugins WHERE plugin_status='ACTIVE'");
                    response.put("activePlugins", plugins);
                } catch (Exception e) {
                    response.put("pluginsError", e.getMessage());
                }
            }
            
            log.info("Informações do {}} recuperadas com sucesso", dbType);
            return ResponseEntity.ok(ApiResponse.builder()
                    .status("SUCCESS")
                    .message("Informações do " + dbType)
                    .data(response)
                    .build());
            
        } catch (Exception e) {
            log.error("Erro ao obter informações do banco de dados: {}", e.getMessage(), e);
            
            response.put("error", e.getMessage());
            response.put("errorType", e.getClass().getName());
            
            return ResponseEntity.ok(ApiResponse.builder()
                    .status("ERROR")
                    .message("Falha ao obter informações do banco de dados")
                    .data(response)
                    .build());
        }
    }
} 