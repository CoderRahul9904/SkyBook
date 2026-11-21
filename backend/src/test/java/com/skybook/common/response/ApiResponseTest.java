package com.skybook.common.response;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ApiResponseTest {

    @Test
    void createsSuccessfulResponseEnvelope() {
        ApiResponse<String> response = ApiResponse.of(200, "Success", "test");

        assertThat(response.status()).isEqualTo(200);
        assertThat(response.message()).isEqualTo("Success");
        assertThat(response.data()).isEqualTo("test");
        assertThat(response.timestamp()).isNotNull();
    }
}
