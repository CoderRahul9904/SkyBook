package com.skybook.booking;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PnrGeneratorTest {

    @Test
    void generatesEightCharacterUppercaseAlphanumericPnr() {
        PnrGenerator generator = new PnrGenerator();
        assertThat(generator.generate()).matches("[A-Z0-9]{8}");
    }
}
