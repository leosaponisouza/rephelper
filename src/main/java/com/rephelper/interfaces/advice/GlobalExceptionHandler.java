package com.rephelper.interfaces.advice;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import com.rephelper.application.dto.response.ErrorResponse;
import com.rephelper.domain.exception.AuthenticationException;
import com.rephelper.domain.exception.BusinessException;
import com.rephelper.domain.exception.ConflictException;
import com.rephelper.domain.exception.DomainException;
import com.rephelper.domain.exception.ForbiddenException;
import com.rephelper.domain.exception.ResourceNotFoundException;
import com.rephelper.domain.exception.ValidationException;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;

/**
 * Manipulador global de exceções que converte exceções em respostas HTTP adequadas
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Trata exceções de validação do Bean Validation
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex, WebRequest request) {

        Map<String, String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        FieldError::getDefaultMessage,
                        (error1, error2) -> error1 + ", " + error2
                ));

        ErrorResponse errorResponse = ErrorResponse.builder()
                .status("VALIDATION_FAILED")
                .message("Validation failed")
                .timestamp(getCurrentTimestamp())
                .path(getRequestPath(request))
                .details(errors)
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Trata exceções de recurso não encontrado
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
            ResourceNotFoundException ex, WebRequest request) {

        ErrorResponse errorResponse = ErrorResponse.builder()
                .status("NOT_FOUND")
                .message(ex.getMessage())
                .timestamp(getCurrentTimestamp())
                .path(getRequestPath(request))
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    /**
     * Trata exceções de validação de domínio
     */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            ValidationException ex, WebRequest request) {

        ErrorResponse errorResponse = ErrorResponse.builder()
                .status("VALIDATION_ERROR")
                .message(ex.getMessage())
                .timestamp(getCurrentTimestamp())
                .path(getRequestPath(request))
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Trata exceções de acesso proibido
     */
    @ExceptionHandler({ForbiddenException.class, AccessDeniedException.class})
    public ResponseEntity<ErrorResponse> handleForbiddenException(
            Exception ex, WebRequest request) {

        ErrorResponse errorResponse = ErrorResponse.builder()
                .status("FORBIDDEN")
                .message(ex.getMessage())
                .timestamp(getCurrentTimestamp())
                .path(getRequestPath(request))
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    /**
     * Trata exceções de autenticação
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(
            AuthenticationException ex, WebRequest request) {

        ErrorResponse errorResponse = ErrorResponse.builder()
                .status("UNAUTHORIZED")
                .message(ex.getMessage())
                .timestamp(getCurrentTimestamp())
                .path(getRequestPath(request))
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Trata exceções de conflito
     */
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflictException(
            ConflictException ex, WebRequest request) {

        ErrorResponse errorResponse = ErrorResponse.builder()
                .status("CONFLICT")
                .message(ex.getMessage())
                .timestamp(getCurrentTimestamp())
                .path(getRequestPath(request))
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    /**
     * Trata exceções de negócio
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(
            BusinessException ex, WebRequest request) {

        ErrorResponse errorResponse = ErrorResponse.builder()
                .status("BUSINESS_ERROR")
                .message(ex.getMessage())
                .timestamp(getCurrentTimestamp())
                .path(getRequestPath(request))
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    /**
     * Trata exceções de JWT
     */
    @ExceptionHandler({
            ExpiredJwtException.class,
            MalformedJwtException.class,
            SignatureException.class,
            IllegalArgumentException.class
    })
    public ResponseEntity<ErrorResponse> handleJwtException(
            Exception ex, WebRequest request) {

        String message;
        if (ex instanceof ExpiredJwtException) {
            message = "JWT token has expired";
        } else if (ex instanceof MalformedJwtException || ex instanceof SignatureException) {
            message = "Invalid JWT token";
        } else {
            message = "JWT error: " + ex.getMessage();
        }

        ErrorResponse errorResponse = ErrorResponse.builder()
                .status("UNAUTHORIZED")
                .message(message)
                .timestamp(getCurrentTimestamp())
                .path(getRequestPath(request))
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Trata todas as outras exceções de domínio
     */
    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ErrorResponse> handleDomainException(
            DomainException ex, WebRequest request) {

        ErrorResponse errorResponse = ErrorResponse.builder()
                .status("DOMAIN_ERROR")
                .message(ex.getMessage())
                .timestamp(getCurrentTimestamp())
                .path(getRequestPath(request))
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Trata todas as outras exceções não mapeadas
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(
            Exception ex, WebRequest request) {

        log.error("Unhandled exception", ex);

        Map<String, Object> details = null;
        // Adiciona detalhes do erro em ambiente de desenvolvimento
        if (isDevelopmentEnvironment()) {
            details = new HashMap<>();
            details.put("exception", ex.getClass().getName());
            details.put("stackTrace", ex.getStackTrace());
        }

        ErrorResponse errorResponse = ErrorResponse.builder()
                .status("INTERNAL_SERVER_ERROR")
                .message("An unexpected error occurred")
                .timestamp(getCurrentTimestamp())
                .path(getRequestPath(request))
                .details(details)
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Verifica se o ambiente é de desenvolvimento
     */
    private boolean isDevelopmentEnvironment() {
        String activeProfile = System.getProperty("spring.profiles.active");
        return activeProfile != null &&
                (activeProfile.equals("dev") || activeProfile.equals("development"));
    }

    /**
     * Obtém o caminho da requisição
     */
    private String getRequestPath(WebRequest request) {
        return request.getDescription(false).substring(4); // Remove "uri="
    }

    /**
     * Obtém o timestamp atual formatado
     */
    private String getCurrentTimestamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);
    }
}