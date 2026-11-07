package com.stw.dto;

import java.time.Instant;

import com.stw.entities.UserEntity;

import jakarta.validation.constraints.*;

public class AuthDto {
    private AuthDto() {}
    public record Credentials(
        @NotBlank 
        String username,
        @NotBlank 
        @Size(min = 10, max = 72) 
        String password
    ) {
            @Override 
            public String toString() { 
                return "Credentials[username=" + username + ", password=REDACTED]"; 
            }
    }
    public record RegisterUser(
        Long id, 
        String username, 
        UserEntity.Role role
    ) {}
    public record Token(
        String accessToken, 
        String tokenType, 
        Instant expiresAt) {
        @Override 
        public String toString() { 
            return "Token[accessToken=REDACTED, tokenType=" + tokenType + ", expiresAt=" + expiresAt + "]"; 
        }
    }
}
