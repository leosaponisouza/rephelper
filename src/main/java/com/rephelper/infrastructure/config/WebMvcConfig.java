package com.rephelper.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class WebMvcConfig implements WebMvcConfigurer {
    
    public WebMvcConfig() {
        log.info("Inicializando WebMvcConfig");
    }
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        log.info("Configurando manipuladores de recursos");
        registry.addResourceHandler("/swagger-ui/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/swagger-ui/4.15.5/");
        registry.addResourceHandler("/api-docs/**")
                .addResourceLocations("classpath:/META-INF/resources/");
    }
    
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        log.info("Configurando controladores de visualização");
        registry.addViewController("/swagger-ui/")
                .setViewName("forward:/swagger-ui/index.html");
    }
    
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        log.info("Configurando mapeamentos CORS");
        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                .allowedHeaders("*")
                .maxAge(3600);
    }
} 