package com.rephelper.infrastructure.adapter.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

/**
 * Implementação customizada de UserDetails
 */
public class CustomUserDetails implements UserDetails {

    private static final long serialVersionUID = 1L;

    private final UUID userId;
    private final String role;
    private final Collection<? extends GrantedAuthority> authorities;

    public CustomUserDetails(UUID userId, String role) {
        this.userId = userId;
        this.role = role;
        this.authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role));
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return null; // Não usamos senha para autenticação JWT
    }

    @Override
    public String getUsername() {
        return userId.toString();
    }

    public UUID getUserId() {
        return userId;
    }

    public String getRole() {
        return role;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
