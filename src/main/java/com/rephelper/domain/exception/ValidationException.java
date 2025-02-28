package com.rephelper.domain.exception;

/**
 * Exceção lançada quando há falha na validação de dados
 */
public class ValidationException extends DomainException {
    public ValidationException(String message) {
        super(message);
    }
}