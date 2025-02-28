package com.rephelper.domain.exception;

/**
 * Exceção lançada quando uma requisição é inválida
 */
public class BadRequestException extends DomainException {
    public BadRequestException(String message) {
        super(message);
    }
}