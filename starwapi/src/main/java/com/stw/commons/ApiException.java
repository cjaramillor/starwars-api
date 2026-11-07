package com.stw.commons;

import org.springframework.http.HttpStatus;

public class ApiException extends RuntimeException {
    
    public final HttpStatus status;
    
    public ApiException(HttpStatus status, String message) { 
        super(message); 
        this.status = status; 
    }
    public static ApiException notFound() { 
        return new ApiException(HttpStatus.NOT_FOUND, "Recurso no encontrado"); 
    }
}
