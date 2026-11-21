package com.skybook.flight;

import com.skybook.airport.AirportRepository;
import com.skybook.common.exception.BusinessRuleViolationException;
import com.skybook.common.exception.ResourceNotFoundException;
import com.skybook.flight.dto.FlightResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.LocalDate;
import java.util.List;

@Service
public class FlightService {

    private final FlightRepository flightRepository;
    private final AirportRepository airportRepository;
    private final FlightMapper flightMapper;

    public FlightService(FlightRepository flightRepository, AirportRepository airportRepository, FlightMapper flightMapper) {
        this.flightRepository = flightRepository;
        this.airportRepository = airportRepository;
        this.flightMapper = flightMapper;
    }

    @Transactional(readOnly = true)
    public List<FlightResponse> search(String originCode, String destinationCode, LocalDate travelDate, int passengerCount, CabinClass cabinClass) {
        if (passengerCount < 1) {
            throw new BusinessRuleViolationException("Passenger count must be positive");
        }
        if (travelDate == null || travelDate.isBefore(LocalDate.now(ZoneOffset.UTC))) {
            throw new BusinessRuleViolationException("Travel date must be today or a future date");
        }
        if (originCode == null || originCode.isBlank() || destinationCode == null || destinationCode.isBlank()) {
            throw new BusinessRuleViolationException("Origin and destination airport codes are required");
        }

        String origin = originCode.trim().toUpperCase();
        String destination = destinationCode.trim().toUpperCase();
        if (origin.equals(destination)) {
            throw new BusinessRuleViolationException("Origin and destination airports must be different");
        }
        airportRepository.findByCodeIgnoreCase(origin)
                .orElseThrow(() -> new ResourceNotFoundException("Origin airport not found"));
        airportRepository.findByCodeIgnoreCase(destination)
                .orElseThrow(() -> new ResourceNotFoundException("Destination airport not found"));

        OffsetDateTime start = travelDate.atStartOfDay().atOffset(ZoneOffset.UTC);
        OffsetDateTime end = travelDate.plusDays(1).atStartOfDay().atOffset(ZoneOffset.UTC);
        return flightRepository.searchDirectFlights(origin, destination, start, end, passengerCount, cabinClass)
                .stream()
                .filter(flight -> flight.getInventories().stream()
                        .anyMatch(inventory -> inventory.getCabinClass() == cabinClass
                                && inventory.getAvailableSeats() >= passengerCount))
                .filter(flight -> flight.getFares().stream()
                        .anyMatch(fare -> fare.getCabinClass() == cabinClass))
                .map(flight -> flightMapper.toSearchResponse(flight, cabinClass))
                .toList();
    }

    @Transactional(readOnly = true)
    public FlightResponse getDetails(Long flightId) {
        Flight flight = flightRepository.findById(flightId)
                .orElseThrow(() -> new ResourceNotFoundException("Flight not found"));
        if (flight.getStatus() == FlightStatus.CANCELLED) {
            throw new BusinessRuleViolationException("Flight is unavailable");
        }
        return flightMapper.toDetailResponse(flight);
    }
}
