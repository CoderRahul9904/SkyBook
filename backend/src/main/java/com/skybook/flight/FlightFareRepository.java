package com.skybook.flight;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FlightFareRepository extends JpaRepository<FlightFare, Long> {
    Optional<FlightFare> findByFlightIdAndCabinClass(Long flightId, CabinClass cabinClass);
}
