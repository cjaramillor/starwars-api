package com.stw.entities;

import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

public class UserEntity {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY) 
    public Long id;
    @Column(nullable = false, unique = true, length = 50) 
    public String username;
    @Column(nullable = false, length = 100) 
    public String passwordHash;
    @Enumerated(EnumType.STRING) 
    @Column(nullable = false, length = 10) 
    public Role role;
    public enum Role { USER, ADMIN }
}
