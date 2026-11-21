package com.skybook.flight;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;

public interface FlightRepository extends JpaRepository<Flight, Long> {

    @Query("""
            select f from Flight f
            join f.origin origin
            join f.destination destination
            where origin.code = :originCode
              and destination.code = :destinationCode
              and f.departureTime >= :start
              and f.departureTime < :end
              and f.status <> com.skybook.flight.FlightStatus.CANCELLED
            order by f.departureTime
            """)
    List<Flight> searchDirectFlights(
            @Param("originCode") String originCode,
            @Param("destinationCode") String destinationCode,
            @Param("start") OffsetDateTime start,
            @Param("end") OffsetDateTime end,
            @Param("passengerCount") int passengerCount,
            @Param("cabinClass") CabinClass cabinClass
    );
}
