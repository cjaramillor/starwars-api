package com.stw.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.stw.dto.AuthDto.Credentials;
import com.stw.dto.AuthDto.RegisterUser;
import com.stw.dto.AuthDto.Token;
import com.stw.service.AuthService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController 
@RequestMapping ("/api/auth")
public class AuthController {

    private final AuthService service;
    public AuthController(AuthService service){
        this.service = service;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public RegisterUser register(@Valid @RequestBody Credentials credentials) {
        return service.register(credentials);
    }

    @PostMapping("/login")
    public Token login(@Valid @RequestBody Credentials credentials) { 
        return service.login(credentials); 
    }
    
}
