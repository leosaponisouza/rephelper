package com.rephelper.domain.exception;

/**
 * Exceção lançada quando ocorre um erro de negócio
 */
public class BusinessException extends DomainException {
    public BusinessException(String message) {
        super(message);
    }
}