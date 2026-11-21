package com.skybook.flight;

import com.skybook.common.response.ApiResponse;
import com.skybook.flight.dto.FlightResponse;
import jakarta.validation.constraints.Min;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/flights")
@Validated
public class FlightController {

    private final FlightService flightService;

    public FlightController(FlightService flightService) {
        this.flightService = flightService;
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<FlightResponse>>> search(
            @RequestParam String origin,
            @RequestParam String destination,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate travelDate,
            @RequestParam @Min(1) int passengerCount,
            @RequestParam CabinClass cabinClass
    ) {
        List<FlightResponse> flights = flightService.search(origin, destination, travelDate, passengerCount, cabinClass);
        return ResponseEntity.ok(ApiResponse.of(HttpStatus.OK.value(), "Flight search completed", flights));
    }

    @GetMapping("/{flightId}")
    public ResponseEntity<ApiResponse<FlightResponse>> getDetails(@PathVariable Long flightId) {
        FlightResponse flight = flightService.getDetails(flightId);
        return ResponseEntity.ok(ApiResponse.of(HttpStatus.OK.value(), "Flight details retrieved", flight));
    }
}
