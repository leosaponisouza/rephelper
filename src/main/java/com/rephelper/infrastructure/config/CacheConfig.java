package com.rephelper.infrastructure.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@EnableCaching
@Profile("prod")
public class CacheConfig {
    // A configuração é feita via application-prod.properties
    // Esta classe apenas habilita o cache para o perfil de produção
} 