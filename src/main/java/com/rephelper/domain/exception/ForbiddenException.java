package com.rephelper.domain.exception;

/**
 * Exceção lançada quando o acesso a um recurso é negado
 */
public class ForbiddenException extends DomainException {
    public ForbiddenException(String message) {
        super(message);
    }
}