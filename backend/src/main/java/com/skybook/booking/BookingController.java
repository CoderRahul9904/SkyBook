package com.skybook.booking;

import com.skybook.auth.User;
import com.skybook.booking.dto.BookingResponse;
import com.skybook.booking.dto.CreateBookingRequest;
import com.skybook.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/bookings")
@Tag(name = "Booking Management")
@SecurityRequirement(name = "bearerAuth")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    @Operation(summary = "Create a booking")
    public ResponseEntity<ApiResponse<BookingResponse>> create(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody CreateBookingRequest request
    ) {
        BookingResponse booking = bookingService.createBooking(user, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.of(HttpStatus.CREATED.value(), "Booking created", booking));
    }

    @GetMapping("/my")
    @Operation(summary = "Get authenticated customer's bookings")
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getMyBookings(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.of(HttpStatus.OK.value(), "Bookings retrieved", bookingService.getMyBookings(user)));
    }

    @GetMapping("/{pnr}")
    @Operation(summary = "Get an owned booking by PNR")
    public ResponseEntity<ApiResponse<BookingResponse>> getByPnr(
            @AuthenticationPrincipal User user,
            @PathVariable String pnr
    ) {
        return ResponseEntity.ok(ApiResponse.of(HttpStatus.OK.value(), "Booking retrieved", bookingService.getByPnr(user, pnr)));
    }

    @PatchMapping("/{pnr}/cancel")
    @Operation(summary = "Cancel an owned confirmed booking")
    public ResponseEntity<ApiResponse<BookingResponse>> cancel(
            @AuthenticationPrincipal User user,
            @PathVariable String pnr
    ) {
        return ResponseEntity.ok(ApiResponse.of(HttpStatus.OK.value(), "Booking cancelled", bookingService.cancel(user, pnr)));
    }
}
