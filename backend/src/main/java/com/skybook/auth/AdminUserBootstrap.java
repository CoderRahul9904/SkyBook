package com.skybook.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AdminUserBootstrap implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminUserBootstrap.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${skybook.admin.bootstrap.enabled:false}")
    private boolean enabled;

    @Value("${skybook.admin.bootstrap.full-name:}")
    private String fullName;

    @Value("${skybook.admin.bootstrap.email:}")
    private String email;

    @Value("${skybook.admin.bootstrap.password:}")
    private String password;

    public AdminUserBootstrap(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!enabled) {
            return;
        }

        if (fullName.isBlank() || email.isBlank() || password.isBlank()) {
            log.warn("Admin bootstrap is enabled but one or more required values are missing");
            return;
        }

        String normalizedEmail = email.trim().toLowerCase();
        User user = userRepository.findByEmailIgnoreCase(normalizedEmail).orElseGet(User::new);
        boolean exists = user.getId() != null;

        user.setFullName(fullName.trim());
        user.setEmail(normalizedEmail);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setRole(UserRole.ADMIN);
        userRepository.save(user);

        if (exists) {
            log.info("Admin bootstrap updated existing user for email={}", normalizedEmail);
        } else {
            log.info("Admin bootstrap created admin user for email={}", normalizedEmail);
        }
    }
}
