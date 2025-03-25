package com.rephelper.infrastructure.config; // Put this in a suitable package

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import lombok.Getter;
import lombok.Setter;


@Configuration
@ConfigurationProperties(prefix = "rephelper.jwt")
@Getter
@Setter
public class JwtProperties {
    private String secret;
    private long expiration;
    private String issuer;
    private String audience;
}