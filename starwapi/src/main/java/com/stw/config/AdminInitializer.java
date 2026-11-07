package com.stw.config;

import com.stw.entities.UserEntity;
import com.stw.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

@Component
public class AdminInitializer implements ApplicationRunner {
    private final UserRepository users;
    private final PasswordEncoder passwords;
    private final String username;
    private final String password;

    public AdminInitializer(UserRepository users, PasswordEncoder passwords,
                            @Value("${app.admin.username:}") String username,
                            @Value("${app.admin.password:}") String password) {
        this.users = users;
        this.passwords = passwords;
        this.username = username;
        this.password = password;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (username.isEmpty() && password.isEmpty()) {
            return;
        }
        if (!username.matches("[a-zA-Z0-9._-]{3,50}") || password.isBlank()
                || password.length() < 10 || password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new IllegalArgumentException("Configura ADMIN_USERNAME y ADMIN_PASSWORD válidos: "
                    + "usuario de 3 a 50 caracteres y contraseña de al menos 10 caracteres y máximo 72 bytes");
        }
        String normalizedUsername = username.toLowerCase(Locale.ROOT);
        UserEntity existing = users.findByUsername(normalizedUsername).orElse(null);
        if (existing != null) {
            if (existing.role != UserEntity.Role.ADMIN) {
                throw new IllegalStateException("El usuario configurado como administrador ya existe con otro rol");
            }
            return;
        }
        UserEntity admin = new UserEntity();
        admin.username = normalizedUsername;
        admin.passwordHash = passwords.encode(password);
        admin.role = UserEntity.Role.ADMIN;
        users.saveAndFlush(admin);
    }
}
