package com.rephelper.infrastructure.adapter.security;

import java.util.*;
import java.util.function.Function;

import javax.crypto.SecretKey;

import com.rephelper.infrastructure.config.JwtProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;

import lombok.extern.slf4j.Slf4j;

/**
 * Provedor de tokens JWT
 */
@Component
@Slf4j
public class JwtTokenProvider {

    private final SecretKey key;
    private final long jwtExpirationMs;
    private final String issuer;
    private final String audience;


    public JwtTokenProvider(JwtProperties jwtProperties) {
        // If the existing secret is not long enough, generate a new secure key
        if (jwtProperties.getSecret() == null || jwtProperties.getSecret().length() < 64) {
            // Generate a secure key
            this.key = Keys.secretKeyFor(SignatureAlgorithm.HS512);

            // Convert the key to a base64 string that can be stored in properties
            String encodedKey = Base64.getEncoder().encodeToString(this.key.getEncoded());
            log.warn("Generated new secure JWT secret key. Please update your configuration with: rephelper.jwt.secret=" + encodedKey);
        } else {
            // Use the provided secret
            this.key = Keys.hmacShaKeyFor(
                    Base64.getDecoder().decode(jwtProperties.getSecret())
            );
        }

        this.jwtExpirationMs = jwtProperties.getExpiration();
        this.issuer = jwtProperties.getIssuer();
        this.audience = jwtProperties.getAudience();
    }


    /**
     * Gera um token JWT para o usuário
     */
    public String generateToken(UUID userId) {
        Map<String, Object> claims = new HashMap<>();

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userId.toString())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .setIssuer(issuer)
                .setAudience(audience)
                .signWith(key, SignatureAlgorithm.HS512) // Usar HS512 em vez de RS256
                .compact();
    }

    /**
     * Obtém o ID do usuário a partir do token
     */
    public UUID getUserIdFromToken(String token) {
        String id = getClaimFromToken(token, Claims::getSubject);
        return UUID.fromString(id);
    }

    /**
     * Obtém o papel do usuário a partir do token
     */
    public String getRoleFromToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        return claims.get("role", String.class);
    }

    /**
     * Obtém a data de expiração do token
     */
    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    /**
     * Obtém uma claim específica do token
     */
    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Obtém todas as claims do token
     */
    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)  // Use the SecretKey here
                .setAllowedClockSkewSeconds(30) // Permite 30 segundos de diferença
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Obtém o ID do usuário a partir do token mesmo que esteja expirado
     */
    public UUID getUserIdFromExpiredToken(String token) {
        try {
            return getUserIdFromToken(token);
        } catch (ExpiredJwtException e) {
            // Se o token estiver expirado, ainda podemos extrair o subject
            String id = e.getClaims().getSubject();
            return UUID.fromString(id);
        }
    }

    /**
     * Verifica se o token expirou
     */
    private Boolean isTokenExpired(String token) {
        try {
            final Date expiration = getExpirationDateFromToken(token);
            // Adiciona tolerância de 30 segundos (adicionando ao invés de subtrair)
            return expiration.before(new Date(System.currentTimeMillis()));
        } catch (ExpiredJwtException e) {
            return true;
        }
    }

    /**
     * Valida o token JWT
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(key)
                .setAllowedClockSkewSeconds(30) // Permite 30 segundos de diferença
                .build()
                .parseClaimsJws(token);
            
            // O parseClaimsJws já verifica a expiração com o clock skew configurado
            return true;
        } catch (ExpiredJwtException e) {
            log.error("JWT token expirou: {}", e.getMessage());
            return false;
        } catch (MalformedJwtException | SignatureException e) {
            log.error("JWT token inválido: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("Erro na validação JWT: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Cria uma autenticação a partir do token
     */
    public Authentication getAuthentication(String token) {
        UUID userId = getUserIdFromToken(token);
        String role = getRoleFromToken(token);

        UserDetails userDetails = new CustomUserDetails(userId, role);

        return new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
    }
}