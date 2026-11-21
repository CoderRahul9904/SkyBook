package com.skybook.admin;

import com.skybook.admin.dto.AdminFlightResponse;
import com.skybook.admin.dto.AirportRequest;
import com.skybook.admin.dto.AirportResponse;
import com.skybook.admin.dto.CabinFareRequest;
import com.skybook.admin.dto.CabinInventoryRequest;
import com.skybook.admin.dto.CabinManagementResponse;
import com.skybook.admin.dto.CreateFlightRequest;
import com.skybook.admin.dto.UpdateFlightRequest;
import com.skybook.booking.dto.BookingResponse;
import com.skybook.common.response.ApiResponse;
import com.skybook.flight.CabinClass;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@Tag(name = "Admin Management")
@PreAuthorize("hasRole(\'ADMIN\')")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping("/airports")
    @Operation(summary = "Create an airport")
    public ResponseEntity<ApiResponse<AirportResponse>> createAirport(@Valid @RequestBody AirportRequest request) {
        AirportResponse response = adminService.createAirport(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(HttpStatus.CREATED.value(), "Airport created", response));
    }

    @GetMapping("/airports")
    public ResponseEntity<ApiResponse<List<AirportResponse>>> getAirports() {
        return ResponseEntity.ok(ApiResponse.of(200, "Airports retrieved", adminService.getAirports()));
    }

    @GetMapping("/airports/{id}")
    public ResponseEntity<ApiResponse<AirportResponse>> getAirport(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.of(200, "Airport retrieved", adminService.getAirport(id)));
    }

    @PutMapping("/airports/{id}")
    public ResponseEntity<ApiResponse<AirportResponse>> updateAirport(@PathVariable Long id, @Valid @RequestBody AirportRequest request) {
        return ResponseEntity.ok(ApiResponse.of(200, "Airport updated", adminService.updateAirport(id, request)));
    }

    @PostMapping("/flights")
    public ResponseEntity<ApiResponse<AdminFlightResponse>> createFlight(@Valid @RequestBody CreateFlightRequest request) {
        AdminFlightResponse response = adminService.createFlight(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(HttpStatus.CREATED.value(), "Flight created", response));
    }

    @GetMapping("/flights")
    public ResponseEntity<ApiResponse<List<AdminFlightResponse>>> getFlights() {
        return ResponseEntity.ok(ApiResponse.of(200, "Flights retrieved", adminService.getFlights()));
    }

    @GetMapping("/flights/{id}")
    public ResponseEntity<ApiResponse<AdminFlightResponse>> getFlight(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.of(200, "Flight retrieved", adminService.getFlight(id)));
    }

    @PutMapping("/flights/{id}")
    public ResponseEntity<ApiResponse<AdminFlightResponse>> updateFlight(@PathVariable Long id, @Valid @RequestBody UpdateFlightRequest request) {
        return ResponseEntity.ok(ApiResponse.of(200, "Flight updated", adminService.updateFlight(id, request)));
    }

    @PutMapping("/flights/{flightId}/fares/{cabinClass}")
    public ResponseEntity<ApiResponse<CabinManagementResponse>> upsertFare(
            @PathVariable Long flightId,
            @PathVariable CabinClass cabinClass,
            @Valid @RequestBody CabinFareRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.of(200, "Fare updated", adminService.upsertFare(flightId, cabinClass, request)));
    }

    @PutMapping("/flights/{flightId}/inventory/{cabinClass}")
    public ResponseEntity<ApiResponse<CabinManagementResponse>> upsertInventory(
            @PathVariable Long flightId,
            @PathVariable CabinClass cabinClass,
            @Valid @RequestBody CabinInventoryRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.of(200, "Inventory updated", adminService.upsertInventory(flightId, cabinClass, request)));
    }

    @GetMapping("/flights/{flightId}/cabins")
    public ResponseEntity<ApiResponse<List<CabinManagementResponse>>> getCabins(@PathVariable Long flightId) {
        return ResponseEntity.ok(ApiResponse.of(200, "Cabin details retrieved", adminService.getCabins(flightId)));
    }

    @GetMapping("/bookings")
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getBookings() {
        return ResponseEntity.ok(ApiResponse.of(200, "Bookings retrieved", adminService.getBookings()));
    }

    @PatchMapping("/bookings/{pnr}/cancel")
    public ResponseEntity<ApiResponse<BookingResponse>> cancelBooking(@PathVariable String pnr) {
        return ResponseEntity.ok(ApiResponse.of(200, "Booking cancelled", adminService.cancelBooking(pnr)));
    }

    @GetMapping("/bookings/{pnr}")
    public ResponseEntity<ApiResponse<BookingResponse>> getBooking(@PathVariable String pnr) {
        return ResponseEntity.ok(ApiResponse.of(200, "Booking retrieved", adminService.getBooking(pnr)));
    }
}
