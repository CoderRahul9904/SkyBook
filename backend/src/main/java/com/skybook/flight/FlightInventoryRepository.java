package com.skybook.flight;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface FlightInventoryRepository extends JpaRepository<FlightInventory, Long> {
    Optional<FlightInventory> findByFlightIdAndCabinClass(Long flightId, CabinClass cabinClass);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from FlightInventory i where i.flight.id = :flightId and i.cabinClass = :cabinClass")
    Optional<FlightInventory> findByFlightIdAndCabinClassForUpdate(
            @Param("flightId") Long flightId,
            @Param("cabinClass") CabinClass cabinClass
    );
}