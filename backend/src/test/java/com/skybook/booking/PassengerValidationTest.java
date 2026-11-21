package com.skybook.booking;

import com.skybook.booking.dto.PassengerRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class PassengerValidationTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void rejectsBlankNameAndFutureBirthDate() {
        PassengerRequest invalid = new PassengerRequest(" ", LocalDate.now().plusDays(1));
        Set<ConstraintViolation<PassengerRequest>> violations = validator.validate(invalid);
        assertThat(violations).isNotEmpty();
    }

    @Test
    void acceptsValidPassenger() {
        PassengerRequest valid = new PassengerRequest("Aarav Sharma", LocalDate.of(1995, 5, 20));
        assertThat(validator.validate(valid)).isEmpty();
    }
}
