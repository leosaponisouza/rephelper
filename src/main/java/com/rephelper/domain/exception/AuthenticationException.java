package com.rephelper.domain.exception;

/**
 * Exceção lançada quando há falha na autenticação
 */
public class AuthenticationException extends DomainException {
    public AuthenticationException(String message) {
        super(message);
    }
}