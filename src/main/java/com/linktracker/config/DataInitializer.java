package com.linktracker.config;

import com.linktracker.entity.AdminUser;
import com.linktracker.repository.AdminUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Ensures a default administrator account exists on first startup, using
 * credentials from configuration ({@code admin.default-username} /
 * {@code admin.default-password}). The password is hashed with BCrypt and
 * never logged. Operators should change the default password after first
 * login.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.default-username:admin}")
    private String defaultUsername;

    @Value("${admin.default-password:changeme123}")
    private String defaultPassword;

    @Override
    public void run(String... args) {
        if (adminUserRepository.findByUsername(defaultUsername).isEmpty()) {
            AdminUser admin = AdminUser.builder()
                    .username(defaultUsername)
                    .passwordHash(passwordEncoder.encode(defaultPassword))
                    .enabled(true)
                    .build();
            adminUserRepository.save(admin);
            log.info("Created default admin user '{}'. Please change the password after first login.",
                    defaultUsername);
        }
    }
}
