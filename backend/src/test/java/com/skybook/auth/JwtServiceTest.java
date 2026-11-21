package com.skybook.auth;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    @Test
    void generatesAndValidatesJwtWithConfiguredExpiration() {
        JwtService jwtService = new JwtService("test-only-secret-key-that-is-at-least-32-characters", 3600000);
        User user = new User();
        user.setEmail("test@example.com");
        user.setRole(UserRole.CUSTOMER);

        String token = jwtService.generateToken(user);
        assertThat(jwtService.isValid(token)).isTrue();
        assertThat(jwtService.extractEmail(token)).isEqualTo("test@example.com");
        assertThat(jwtService.getExpirationSeconds()).isEqualTo(3600);
    }
}
