package com.rephelper.domain.exception;

/**
 * Exceção lançada quando ocorre um conflito de dados
 */
public class ConflictException extends DomainException {
    public ConflictException(String message) {
        super(message);
    }
}