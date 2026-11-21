package com.skybook.admin;

import com.skybook.admin.dto.AirportRequest;
import com.skybook.admin.dto.CabinFareRequest;
import com.skybook.admin.dto.CabinInventoryRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class AdminDtoValidationTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void rejectsBlankAirportFields() {
        AirportRequest request = new AirportRequest(" ", "", "", "");
        Set<ConstraintViolation<AirportRequest>> violations = validator.validate(request);
        assertThat(violations).isNotEmpty();
    }

    @Test
    void rejectsNegativeFareAndNegativeInventory() {
        Set<ConstraintViolation<CabinFareRequest>> fareViolations =
                validator.validate(new CabinFareRequest(new BigDecimal("-1.00")));
        Set<ConstraintViolation<CabinInventoryRequest>> inventoryViolations =
                validator.validate(new CabinInventoryRequest(-1, -2));

        assertThat(fareViolations).isNotEmpty();
        assertThat(inventoryViolations).isNotEmpty();
    }

    @Test
    void acceptsValidAdminValues() {
        assertThat(validator.validate(new AirportRequest("DEL", "Delhi Airport", "Delhi", "India"))).isEmpty();
        assertThat(validator.validate(new CabinFareRequest(new BigDecimal("6500.00")))).isEmpty();
        assertThat(validator.validate(new CabinInventoryRequest(180, 120))).isEmpty();
    }
}
