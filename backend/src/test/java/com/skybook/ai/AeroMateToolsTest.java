package com.skybook.ai;

import com.skybook.auth.User;
import com.skybook.booking.BookingService;
import com.skybook.booking.dto.BookingResponse;
import com.skybook.common.exception.BusinessRuleViolationException;
import com.skybook.common.exception.UnauthorizedException;
import com.skybook.flight.FlightService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AeroMateToolsTest {

    private FlightService flightService;
    private BookingService bookingService;
    private AeroMateTools tools;

    @BeforeEach
    void setUp() {
        flightService = mock(FlightService.class);
        bookingService = mock(BookingService.class);
        tools = new AeroMateTools(flightService, bookingService);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getMyBookingsUsesAuthenticatedUser() {
        User user = new User();
        user.setId(11L);
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(user, null));

        BookingResponse response = mock(BookingResponse.class);
        when(bookingService.getMyBookings(user)).thenReturn(List.of(response));

        List<BookingResponse> bookings = tools.getMyBookings();

        assertThat(bookings).hasSize(1);
    }

    @Test
    void getMyBookingsFailsWithoutAuthentication() {
        assertThatThrownBy(() -> tools.getMyBookings())
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void getBookingDetailsValidatesPnr() {
        assertThatThrownBy(() -> tools.getBookingDetails(" "))
                .isInstanceOf(BusinessRuleViolationException.class);
    }
}
