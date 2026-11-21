package com.skybook.auth;

import com.skybook.auth.dto.RegisterRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class AuthValidationTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void rejectsWeakPasswordAndAcceptsApprovedPassword() {
        RegisterRequest weak = new RegisterRequest("Test User", "test@example.com", "password");
        Set<ConstraintViolation<RegisterRequest>> weakViolations = validator.validate(weak);
        assertThat(weakViolations).isNotEmpty();

        RegisterRequest strong = new RegisterRequest("Test User", "test@example.com", "Password1!");
        assertThat(validator.validate(strong)).isEmpty();
    }
}
