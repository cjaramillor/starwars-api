package com.stw.commons;

import java.nio.file.AccessDeniedException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;

public class ExceptionHandlerService {
    private static final Logger log = LoggerFactory.getLogger(ExceptionHandlerService.class);
    
    private ResponseEntity<ApiError> error(HttpStatus status, String message, HttpServletRequest req) {
        return ResponseEntity.status(status).body(ApiError.of(status.value(), message, req.getRequestURI()));
    }
    @ExceptionHandler(ApiException.class)
    ResponseEntity<ApiError> api(ApiException ex, HttpServletRequest req) { 
        return error(ex.status, ex.getMessage(), req); 
    }
    @ExceptionHandler(
        {
            MethodArgumentNotValidException.class, 
            ConstraintViolationException.class,
            HandlerMethodValidationException.class, 
            IllegalArgumentException.class
        })
    ResponseEntity<ApiError> badRequest(Exception ex, HttpServletRequest req) {
        return error(HttpStatus.BAD_REQUEST, "Solicitud inválida: verifica los campos y parámetros", req);
    }
    
    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<ApiError> forbidden(Exception ex, HttpServletRequest req) {
        return error(HttpStatus.FORBIDDEN, "Sin permisos", req); 
    }
    
    @ExceptionHandler(NoResourceFoundException.class)
    ResponseEntity<ApiError> missing(Exception ex, HttpServletRequest req) { 
        return error(HttpStatus.NOT_FOUND, "Recurso no encontrado", req); 
    }
    
    @ExceptionHandler(ResponseStatusException.class)
    ResponseEntity<ApiError> status(ResponseStatusException ex, HttpServletRequest req) {
        return error(HttpStatus.valueOf(ex.getStatusCode().value()), "Solicitud rechazada", req);
    }
    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiError> unexpected(Exception ex, HttpServletRequest req) {
        log.error("Error procesando {}", req.getRequestURI(), ex);
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor", req);
    }
}
