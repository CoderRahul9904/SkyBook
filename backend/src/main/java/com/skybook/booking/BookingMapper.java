package com.skybook.booking;

import com.skybook.booking.dto.BookingFlightResponse;
import com.skybook.booking.dto.BookingResponse;
import com.skybook.booking.dto.PassengerResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BookingMapper {

    public BookingResponse toResponse(Booking booking) {
        BookingFlightResponse flight = new BookingFlightResponse(
                booking.getFlight().getId(),
                booking.getFlight().getAirlineName(),
                booking.getFlight().getAirlineCode(),
                booking.getFlight().getFlightNumber(),
                booking.getFlight().getOrigin().getCode(),
                booking.getFlight().getDestination().getCode(),
                booking.getFlight().getDepartureTime(),
                booking.getFlight().getArrivalTime()
        );
        List<PassengerResponse> passengers = booking.getPassengers().stream()
                .map(passenger -> new PassengerResponse(passenger.getFullName(), passenger.getBirthDate()))
                .toList();
        return new BookingResponse(
                booking.getId(), booking.getPnr(), flight, booking.getCabinClass(),
                booking.getPassengerCount(), booking.getFareSnapshot(), booking.getStatus(),
                booking.getCreatedAt(), booking.getUpdatedAt(), passengers
        );
    }
}
