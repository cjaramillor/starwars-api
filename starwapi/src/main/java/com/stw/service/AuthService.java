package com.stw.service;

import java.time.Duration;
import java.time.Instant;
import java.util.Locale;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.beans.factory.annotation.Value;


import com.stw.commons.ApiException;
import com.stw.dto.AuthDto.Credentials;
import com.stw.dto.AuthDto.RegisterUser;
import com.stw.dto.AuthDto.Token;
import com.stw.entities.UserEntity;
import com.stw.repository.UserRepository;

public class AuthService {

    private final UserRepository repository;
    private final PasswordEncoder passwords;
    private final JwtEncoder encoder;
    private final String dummy;
    private final Duration expiration;

    public AuthService(UserRepository userRepo, 
                       PasswordEncoder passwords, 
                       JwtEncoder encoder,
                       @Value("${app.jwt.expiration}") Duration expiration){
        
                        if (expiration.isNegative() || expiration.isZero()) throw new IllegalArgumentException("JWT_EXPIRATION debe ser positiva");

        this.repository = userRepo;
        this.passwords = passwords;
        this.encoder = encoder;
        this.expiration = expiration;
        this.dummy = passwords.encode("dummy-password");
    }

    @Transactional 
    public RegisterUser register(Credentials request){
        String username = request.username().toLowerCase(Locale.ROOT);
        if(repository.existsByUsername(username)){
            throw new ApiException(HttpStatus.CONFLICT, "El usuario ya existe.");
        }
        UserEntity user = new UserEntity();
        user.username = username;
        user.passwordHash = passwords.encode(request.password());
        user.role = UserEntity.Role.USER;
        repository.saveAndFlush(user);
        return new RegisterUser(user.id, user.username, user.role);
    }

    @Transactional 
    public Token login(Credentials request){
        UserEntity user = repository.findByUsername(request.username().toLowerCase(Locale.ROOT)).orElse(null);
        boolean match = passwords.matches(request.password(), user == null ? dummy : user.passwordHash);
        if(user == null || !match){
            throw new ApiException(HttpStatus.UNAUTHORIZED,"Credenciales invalidas.");
        }

        Instant now = Instant.now();
        Instant expire = now.plus(expiration);
        JwtClaimsSet claims = JwtClaimsSet.builder()
        .issuer("starwapi")
        .subject(user.username)
        .issuedAt(now)
        .expiresAt(expire)
        .claim("role", user.role.name()).build();
        String token = encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(),claims)).getTokenValue();
        return new Token(token, "Bearer", expire);
    }
}
