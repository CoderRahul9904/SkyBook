package com.skybook.ai;

import com.skybook.auth.User;
import com.skybook.booking.BookingService;
import com.skybook.booking.dto.BookingResponse;
import com.skybook.common.exception.BusinessRuleViolationException;
import com.skybook.common.exception.UnauthorizedException;
import com.skybook.flight.CabinClass;
import com.skybook.flight.FlightService;
import com.skybook.flight.dto.FlightResponse;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

@Component
public class AeroMateTools {

    private final FlightService flightService;
    private final BookingService bookingService;

    public AeroMateTools(FlightService flightService, BookingService bookingService) {
        this.flightService = flightService;
        this.bookingService = bookingService;
    }

    @Tool(description = "Search available flights by origin, destination, travelDate (YYYY-MM-DD), passengerCount, and cabinClass (ECONOMY or BUSINESS).")
    @Transactional(readOnly = true)
    public List<FlightResponse> searchFlights(String origin, String destination, String travelDate, Integer passengerCount, String cabinClass) {
        if (passengerCount == null || passengerCount < 1) {
            throw new BusinessRuleViolationException("passengerCount must be at least 1");
        }

        LocalDate date;
        try {
            date = LocalDate.parse(travelDate);
        }
        catch (DateTimeParseException ex) {
            throw new BusinessRuleViolationException("travelDate must follow YYYY-MM-DD");
        }

        CabinClass parsedCabinClass;
        try {
            parsedCabinClass = CabinClass.valueOf(cabinClass.trim().toUpperCase());
        }
        catch (Exception ex) {
            throw new BusinessRuleViolationException("cabinClass must be ECONOMY or BUSINESS");
        }

        return flightService.search(origin, destination, date, passengerCount, parsedCabinClass);
    }

    @Tool(description = "Get full details for a specific flight by flightId.")
    @Transactional(readOnly = true)
    public FlightResponse getFlightDetails(Long flightId) {
        if (flightId == null || flightId < 1) {
            throw new BusinessRuleViolationException("flightId must be a positive number");
        }
        return flightService.getDetails(flightId);
    }

    @Tool(description = "Get all bookings for the authenticated customer.")
    @Transactional(readOnly = true)
    public List<BookingResponse> getMyBookings() {
        return bookingService.getMyBookings(requireAuthenticatedUser());
    }

    @Tool(description = "Get a booking by PNR for the authenticated customer.")
    @Transactional(readOnly = true)
    public BookingResponse getBookingDetails(String pnr) {
        if (pnr == null || pnr.isBlank()) {
            throw new BusinessRuleViolationException("pnr is required");
        }
        return bookingService.getByPnr(requireAuthenticatedUser(), pnr.trim());
    }

    private User requireAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof User user)) {
            throw new UnauthorizedException("Authentication is required");
        }
        return user;
    }
}
