package com.skybook.booking;

import com.skybook.auth.User;
import com.skybook.booking.dto.CreateBookingRequest;
import com.skybook.booking.dto.BookingResponse;
import com.skybook.common.exception.BusinessRuleViolationException;
import com.skybook.common.exception.ConflictException;
import com.skybook.common.exception.ForbiddenException;
import com.skybook.common.exception.ResourceNotFoundException;
import com.skybook.common.exception.UnauthorizedException;
import com.skybook.flight.CabinClass;
import com.skybook.flight.Flight;
import com.skybook.flight.FlightFare;
import com.skybook.flight.FlightFareRepository;
import com.skybook.flight.FlightInventory;
import com.skybook.flight.FlightInventoryRepository;
import com.skybook.flight.FlightRepository;
import com.skybook.flight.FlightStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final BookingMapper bookingMapper;
    private final FlightRepository flightRepository;
    private final FlightFareRepository flightFareRepository;
    private final FlightInventoryRepository flightInventoryRepository;
    private final PnrGenerator pnrGenerator;

    public BookingService(BookingRepository bookingRepository, BookingMapper bookingMapper,
                          FlightRepository flightRepository, FlightFareRepository flightFareRepository,
                          FlightInventoryRepository flightInventoryRepository, PnrGenerator pnrGenerator) {
        this.bookingRepository = bookingRepository;
        this.bookingMapper = bookingMapper;
        this.flightRepository = flightRepository;
        this.flightFareRepository = flightFareRepository;
        this.flightInventoryRepository = flightInventoryRepository;
        this.pnrGenerator = pnrGenerator;
    }

    @Transactional
    public BookingResponse createBooking(User user, CreateBookingRequest request) {
        if (user == null) {
            throw new UnauthorizedException("Authentication is required");
        }
        if (request.passengers() == null || request.passengers().isEmpty()) {
            throw new BusinessRuleViolationException("At least one passenger is required");
        }

        int passengerCount = request.passengers().size();
        for (var passenger : request.passengers()) {
            if (passenger.fullName() == null || passenger.fullName().trim().isEmpty()) {
                throw new BusinessRuleViolationException("Passenger full name is required");
            }
            if (passenger.birthDate() == null || !passenger.birthDate().isBefore(LocalDate.now())) {
                throw new BusinessRuleViolationException("Passenger birth date must be a valid past date");
            }
        }

        CabinClass cabinClass = request.cabinClass();
        if (cabinClass != CabinClass.ECONOMY && cabinClass != CabinClass.BUSINESS) {
            throw new BusinessRuleViolationException("Cabin class must be ECONOMY or BUSINESS");
        }

        Flight flight = flightRepository.findById(request.flightId())
                .orElseThrow(() -> new ResourceNotFoundException("Flight not found"));
        if (flight.getStatus() == FlightStatus.CANCELLED) {
            throw new BusinessRuleViolationException("Flight is unavailable");
        }
        if (flight.getDepartureTime().isBefore(OffsetDateTime.now())) {
            throw new BusinessRuleViolationException("Cannot book a departed flight");
        }

        FlightFare fare = flightFareRepository.findByFlightIdAndCabinClass(request.flightId(), cabinClass)
                .orElseThrow(() -> new ResourceNotFoundException("Cabin fare not found"));
        FlightInventory inventory = flightInventoryRepository
                .findByFlightIdAndCabinClassForUpdate(request.flightId(), cabinClass)
                .orElseThrow(() -> new ResourceNotFoundException("Cabin inventory not found"));

        if (inventory.getAvailableSeats() < passengerCount) {
            throw new ConflictException("Insufficient seats available for this cabin");
        }

        String pnr;
        do {
            pnr = pnrGenerator.generate();
        } while (bookingRepository.existsByPnr(pnr));

        BigDecimal totalFare = fare.getAmount()
                .multiply(BigDecimal.valueOf(passengerCount))
                .setScale(2, java.math.RoundingMode.HALF_UP);

        Booking booking = new Booking();
        booking.setPnr(pnr);
        booking.setCustomer(user);
        booking.setFlight(flight);
        booking.setCabinClass(cabinClass);
        booking.setPassengerCount(passengerCount);
        booking.setFareSnapshot(totalFare);
        booking.setStatus(BookingStatus.CONFIRMED);

        for (var passengerRequest : request.passengers()) {
            BookingPassenger passenger = new BookingPassenger();
            passenger.setBooking(booking);
            passenger.setFullName(passengerRequest.fullName().trim());
            passenger.setBirthDate(passengerRequest.birthDate());
            booking.getPassengers().add(passenger);
        }

        inventory.setAvailableSeats(inventory.getAvailableSeats() - passengerCount);
        bookingRepository.save(booking);
        flightInventoryRepository.save(inventory);

        return bookingMapper.toResponse(booking);
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getMyBookings(User user) {
        if (user == null) {
            throw new UnauthorizedException("Authentication is required");
        }
        return bookingRepository.findByCustomerIdOrderByCreatedAtDesc(user.getId()).stream()
                .map(bookingMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public BookingResponse getByPnr(User user, String pnr) {
        if (user == null) {
            throw new UnauthorizedException("Authentication is required");
        }
        Booking booking = bookingRepository.findByPnr(pnr)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
        if (!booking.getCustomer().getId().equals(user.getId())) {
            throw new ForbiddenException("You cannot access another customer's booking");
        }
        return bookingMapper.toResponse(booking);
    }

    @Transactional
    public BookingResponse cancel(User user, String pnr) {
        if (user == null) {
            throw new UnauthorizedException("Authentication is required");
        }
        return cancelBooking(pnr, user.getId());
    }

    @Transactional
    public BookingResponse cancelForAdmin(String pnr) {
        return cancelBooking(pnr, null);
    }

    private BookingResponse cancelBooking(String pnr, Long ownerUserId) {
        Booking booking = bookingRepository.findByPnrForUpdate(pnr)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
        if (ownerUserId != null && !booking.getCustomer().getId().equals(ownerUserId)) {
            throw new ForbiddenException("You cannot cancel another customer booking");
        }
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new ConflictException("Booking is already cancelled");
        }

        OffsetDateTime cutoff = booking.getFlight().getDepartureTime().minusHours(2);
        if (!OffsetDateTime.now().isBefore(cutoff)) {
            throw new BusinessRuleViolationException("Cancellation is allowed only more than two hours before departure");
        }

        FlightInventory inventory = flightInventoryRepository
                .findByFlightIdAndCabinClassForUpdate(booking.getFlight().getId(), booking.getCabinClass())
                .orElseThrow(() -> new ResourceNotFoundException("Cabin inventory not found"));

        int restoredSeats = inventory.getAvailableSeats() + booking.getPassengerCount();
        if (restoredSeats > inventory.getTotalSeats()) {
            throw new ConflictException("Inventory restoration would exceed cabin capacity");
        }

        inventory.setAvailableSeats(restoredSeats);
        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);
        flightInventoryRepository.save(inventory);
        return bookingMapper.toResponse(booking);
    }
}
