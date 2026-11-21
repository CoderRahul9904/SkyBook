package com.skybook.flight;

import com.skybook.airport.Airport;
import com.skybook.flight.dto.AirportResponse;
import com.skybook.flight.dto.CabinAvailabilityResponse;
import com.skybook.flight.dto.FlightResponse;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;

@Component
public class FlightMapper {

    public AirportResponse toAirportResponse(Airport airport) {
        return new AirportResponse(airport.getId(), airport.getCode(), airport.getName(), airport.getCity(), airport.getCountry());
    }

    public FlightResponse toSearchResponse(Flight flight, CabinClass cabinClass) {
        FlightInventory inventory = flight.getInventories().stream()
                .filter(item -> item.getCabinClass() == cabinClass)
                .findFirst()
                .orElseThrow();
        FlightFare fare = flight.getFares().stream()
                .filter(item -> item.getCabinClass() == cabinClass)
                .findFirst()
                .orElseThrow();
        return toResponse(flight, cabinClass, fare, inventory);
    }

    public FlightResponse toDetailResponse(Flight flight) {
        FlightInventory firstInventory = flight.getInventories().stream()
                .filter(item -> item.getCabinClass() == CabinClass.ECONOMY)
                .findFirst()
                .orElseGet(() -> flight.getInventories().stream().findFirst().orElseThrow());
        FlightFare firstFare = flight.getFares().stream()
                .filter(item -> item.getCabinClass() == firstInventory.getCabinClass())
                .findFirst()
                .orElseThrow();
        return toResponse(flight, firstInventory.getCabinClass(), firstFare, firstInventory);
    }

    private FlightResponse toResponse(Flight flight, CabinClass cabinClass, FlightFare selectedFare, FlightInventory selectedInventory) {
        List<CabinAvailabilityResponse> cabins = flight.getInventories().stream()
                .sorted(Comparator.comparing(FlightInventory::getCabinClass))
                .map(inventory -> {
                    FlightFare fare = flight.getFares().stream()
                            .filter(item -> item.getCabinClass() == inventory.getCabinClass())
                            .findFirst()
                            .orElseThrow();
                    return new CabinAvailabilityResponse(inventory.getCabinClass(), fare.getAmount(), inventory.getTotalSeats(), inventory.getAvailableSeats());
                })
                .toList();

        return new FlightResponse(
                flight.getId(),
                flight.getAirlineName(),
                flight.getAirlineCode(),
                flight.getFlightNumber(),
                toAirportResponse(flight.getOrigin()),
                toAirportResponse(flight.getDestination()),
                flight.getDepartureTime(),
                flight.getArrivalTime(),
                Duration.between(flight.getDepartureTime(), flight.getArrivalTime()).toMinutes(),
                0,
                cabinClass,
                selectedFare.getAmount(),
                selectedInventory.getAvailableSeats(),
                flight.getStatus(),
                cabins
        );
    }
}
