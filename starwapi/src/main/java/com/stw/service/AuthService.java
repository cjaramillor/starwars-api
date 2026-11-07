package com.stw.service;

import java.util.Locale;

import org.springframework.transaction.annotation.Transactional;

import com.stw.dto.AuthDto.Credentials;
import com.stw.dto.AuthDto.RegisterUser;
import com.stw.entities.UserEntity;
import com.stw.repository.UserRepository;

public class AuthService {

    private final UserRepository repository;

    public AuthService(UserRepository userRepo){
        this.repository = userRepo;
    }

    @Transactional 
    public RegisterUser register(Credentials request){
        String username = request.username().toLowerCase(Locale.ROOT);
        if(repository.existsByUsername(username)){
            
        }
        UserEntity user = new UserEntity();
        user.username = "";
        user.passwordHash = "";
        repository.saveAndFlush(user);
        return new RegisterUser(user.id, user.username, user.role);
    }
}
