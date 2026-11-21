package com.skybook.admin;

import com.skybook.admin.dto.AdminFlightResponse;
import com.skybook.admin.dto.AirportRequest;
import com.skybook.admin.dto.AirportResponse;
import com.skybook.admin.dto.CabinFareRequest;
import com.skybook.admin.dto.CabinInventoryRequest;
import com.skybook.admin.dto.CabinManagementResponse;
import com.skybook.admin.dto.CreateFlightRequest;
import com.skybook.admin.dto.UpdateFlightRequest;
import com.skybook.airport.Airport;
import com.skybook.airport.AirportRepository;
import com.skybook.booking.Booking;
import com.skybook.booking.BookingMapper;
import com.skybook.booking.BookingRepository;
import com.skybook.booking.BookingService;
import com.skybook.booking.dto.BookingResponse;
import com.skybook.common.exception.BusinessRuleViolationException;
import com.skybook.common.exception.ConflictException;
import com.skybook.common.exception.ResourceNotFoundException;
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

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class AdminService {

    private final AirportRepository airportRepository;
    private final FlightRepository flightRepository;
    private final FlightFareRepository flightFareRepository;
    private final FlightInventoryRepository flightInventoryRepository;
    private final BookingRepository bookingRepository;
    private final BookingMapper bookingMapper;
    private final BookingService bookingService;

    public AdminService(AirportRepository airportRepository, FlightRepository flightRepository,
                        FlightFareRepository flightFareRepository, FlightInventoryRepository flightInventoryRepository,
                        BookingRepository bookingRepository, BookingMapper bookingMapper, BookingService bookingService) {
        this.airportRepository = airportRepository;
        this.flightRepository = flightRepository;
        this.flightFareRepository = flightFareRepository;
        this.flightInventoryRepository = flightInventoryRepository;
        this.bookingRepository = bookingRepository;
        this.bookingMapper = bookingMapper;
        this.bookingService = bookingService;
    }

    @Transactional
    public AirportResponse createAirport(AirportRequest request) {
        String code = normalizeAirportCode(request.code());
        if (airportRepository.findByCodeIgnoreCase(code).isPresent()) {
            throw new ConflictException("Airport code already exists");
        }
        Airport airport = new Airport();
        airport.setCode(code);
        airport.setName(request.name().trim());
        airport.setCity(request.city().trim());
        airport.setCountry(request.country().trim());
        return toAirportResponse(airportRepository.save(airport));
    }

    @Transactional(readOnly = true)
    public List<AirportResponse> getAirports() {
        return airportRepository.findAll().stream().map(this::toAirportResponse).toList();
    }

    @Transactional(readOnly = true)
    public AirportResponse getAirport(Long id) {
        return toAirportResponse(getAirportEntity(id));
    }

    @Transactional
    public AirportResponse updateAirport(Long id, AirportRequest request) {
        Airport airport = getAirportEntity(id);
        String code = normalizeAirportCode(request.code());
        airportRepository.findByCodeIgnoreCase(code).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new ConflictException("Airport code already exists");
            }
        });
        airport.setCode(code);
        airport.setName(request.name().trim());
        airport.setCity(request.city().trim());
        airport.setCountry(request.country().trim());
        return toAirportResponse(airportRepository.save(airport));
    }

    @Transactional
    public AdminFlightResponse createFlight(CreateFlightRequest request) {
        validateFlightRequest(request.originAirportId(), request.destinationAirportId(), request.departureTime(), request.arrivalTime());
        Airport origin = getAirportEntity(request.originAirportId());
        Airport destination = getAirportEntity(request.destinationAirportId());

        Flight flight = new Flight();
        flight.setAirlineName(request.airlineName().trim());
        flight.setAirlineCode(request.airlineCode().trim().toUpperCase());
        flight.setFlightNumber(request.flightNumber().trim().toUpperCase());
        flight.setOrigin(origin);
        flight.setDestination(destination);
        flight.setDepartureTime(request.departureTime());
        flight.setArrivalTime(request.arrivalTime());
        flight.setStatus(request.status() == null ? FlightStatus.SCHEDULED : request.status());
        return toFlightResponse(flightRepository.save(flight));
    }

    @Transactional(readOnly = true)
    public List<AdminFlightResponse> getFlights() {
        return flightRepository.findAll().stream().map(this::toFlightResponse).toList();
    }

    @Transactional(readOnly = true)
    public AdminFlightResponse getFlight(Long id) {
        return toFlightResponse(getFlightEntity(id));
    }

    @Transactional
    public AdminFlightResponse updateFlight(Long id, UpdateFlightRequest request) {
        Flight flight = getFlightEntity(id);
        validateFlightRequest(request.originAirportId(), request.destinationAirportId(), request.departureTime(), request.arrivalTime());

        boolean hasBookings = bookingRepository.findAll().stream().anyMatch(booking -> booking.getFlight().getId().equals(id));
        if (hasBookings) {
            throw new ConflictException("Flight cannot be changed because confirmed or historical bookings exist");
        }

        flight.setAirlineName(request.airlineName().trim());
        flight.setAirlineCode(request.airlineCode().trim().toUpperCase());
        flight.setFlightNumber(request.flightNumber().trim().toUpperCase());
        flight.setOrigin(getAirportEntity(request.originAirportId()));
        flight.setDestination(getAirportEntity(request.destinationAirportId()));
        flight.setDepartureTime(request.departureTime());
        flight.setArrivalTime(request.arrivalTime());
        flight.setStatus(request.status());
        return toFlightResponse(flightRepository.save(flight));
    }

    @Transactional
    public CabinManagementResponse upsertFare(Long flightId, CabinClass cabinClass, CabinFareRequest request) {
        getFlightEntity(flightId);
        validateCabin(cabinClass);
        FlightFare fare = flightFareRepository.findByFlightIdAndCabinClass(flightId, cabinClass).orElseGet(() -> {
            FlightFare created = new FlightFare();
            created.setFlight(getFlightEntity(flightId));
            created.setCabinClass(cabinClass);
            return created;
        });
        fare.setAmount(request.amount().setScale(2, java.math.RoundingMode.HALF_UP));
        flightFareRepository.save(fare);
        FlightInventory inventory = flightInventoryRepository.findByFlightIdAndCabinClass(flightId, cabinClass).orElse(null);
        return new CabinManagementResponse(cabinClass, fare.getAmount(), inventory == null ? null : inventory.getTotalSeats(), inventory == null ? null : inventory.getAvailableSeats());
    }

    @Transactional
    public CabinManagementResponse upsertInventory(Long flightId, CabinClass cabinClass, CabinInventoryRequest request) {
        getFlightEntity(flightId);
        validateCabin(cabinClass);
        if (request.availableSeats() > request.totalSeats()) {
            throw new BusinessRuleViolationException("Available seats cannot exceed total seats");
        }

        FlightInventory inventory = flightInventoryRepository.findByFlightIdAndCabinClassForUpdate(flightId, cabinClass).orElseGet(() -> {
            FlightInventory created = new FlightInventory();
            created.setFlight(getFlightEntity(flightId));
            created.setCabinClass(cabinClass);
            return created;
        });

        if (inventory.getId() != null) {
            int reservedSeats = inventory.getTotalSeats() - inventory.getAvailableSeats();
            if (request.totalSeats() < reservedSeats) {
                throw new ConflictException("Total seats cannot be lower than seats already reserved");
            }
            if (request.availableSeats() < 0) {
                throw new BusinessRuleViolationException("Available seats cannot be negative");
            }
        }

        inventory.setTotalSeats(request.totalSeats());
        inventory.setAvailableSeats(request.availableSeats());
        flightInventoryRepository.save(inventory);
        FlightFare fare = flightFareRepository.findByFlightIdAndCabinClass(flightId, cabinClass).orElse(null);
        return new CabinManagementResponse(cabinClass, fare == null ? null : fare.getAmount(), inventory.getTotalSeats(), inventory.getAvailableSeats());
    }

    @Transactional(readOnly = true)
    public List<CabinManagementResponse> getCabins(Long flightId) {
        getFlightEntity(flightId);
        List<CabinManagementResponse> cabins = new ArrayList<>();
        for (CabinClass cabin : List.of(CabinClass.ECONOMY, CabinClass.BUSINESS)) {
            FlightFare fare = flightFareRepository.findByFlightIdAndCabinClass(flightId, cabin).orElse(null);
            FlightInventory inventory = flightInventoryRepository.findByFlightIdAndCabinClass(flightId, cabin).orElse(null);
            if (fare != null || inventory != null) {
                cabins.add(new CabinManagementResponse(cabin, fare == null ? null : fare.getAmount(), inventory == null ? null : inventory.getTotalSeats(), inventory == null ? null : inventory.getAvailableSeats()));
            }
        }
        return cabins;
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getBookings() {
        return bookingRepository.findAll().stream().sorted(Comparator.comparing(Booking::getCreatedAt).reversed()).map(bookingMapper::toResponse).toList();
    }

    @Transactional
    public BookingResponse cancelBooking(String pnr) {
        return bookingService.cancelForAdmin(pnr);
    }

    @Transactional(readOnly = true)
    public BookingResponse getBooking(String pnr) {
        Booking booking = bookingRepository.findByPnr(pnr).orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
        return bookingMapper.toResponse(booking);
    }

    private Airport getAirportEntity(Long id) {
        return airportRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Airport not found"));
    }

    private Flight getFlightEntity(Long id) {
        return flightRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Flight not found"));
    }

    private void validateFlightRequest(Long originId, Long destinationId, OffsetDateTime departureTime, OffsetDateTime arrivalTime) {
        if (originId.equals(destinationId)) {
            throw new BusinessRuleViolationException("Origin and destination airports must be different");
        }
        if (arrivalTime == null || departureTime == null || !arrivalTime.isAfter(departureTime)) {
            throw new BusinessRuleViolationException("Arrival time must be after departure time");
        }
    }

    private void validateCabin(CabinClass cabinClass) {
        if (cabinClass != CabinClass.ECONOMY && cabinClass != CabinClass.BUSINESS) {
            throw new BusinessRuleViolationException("Cabin class must be ECONOMY or BUSINESS");
        }
    }

    private String normalizeAirportCode(String code) {
        if (code == null || code.isBlank()) {
            throw new BusinessRuleViolationException("Airport code is required");
        }
        return code.trim().toUpperCase();
    }

    private AirportResponse toAirportResponse(Airport airport) {
        return new AirportResponse(airport.getId(), airport.getCode(), airport.getName(), airport.getCity(), airport.getCountry(), airport.getCreatedAt(), airport.getUpdatedAt());
    }

    private OffsetDateTime airportCreatedAt(Airport airport) {
        return airport.getId() == null ? null : null;
    }

    private OffsetDateTime airportUpdatedAt(Airport airport) {
        return airport.getId() == null ? null : null;
    }

    private AdminFlightResponse toFlightResponse(Flight flight) {
        AirportResponse origin = toAirportResponse(flight.getOrigin());
        AirportResponse destination = toAirportResponse(flight.getDestination());
        List<CabinManagementResponse> cabins = getCabinsForFlight(flight.getId());
        return new AdminFlightResponse(flight.getId(), flight.getAirlineName(), flight.getAirlineCode(), flight.getFlightNumber(), origin, destination, flight.getDepartureTime(), flight.getArrivalTime(), flight.getStatus(), cabins);
    }

    private List<CabinManagementResponse> getCabinsForFlight(Long flightId) {
        List<CabinManagementResponse> cabins = new ArrayList<>();
        for (CabinClass cabin : List.of(CabinClass.ECONOMY, CabinClass.BUSINESS)) {
            FlightFare fare = flightFareRepository.findByFlightIdAndCabinClass(flightId, cabin).orElse(null);
            FlightInventory inventory = flightInventoryRepository.findByFlightIdAndCabinClass(flightId, cabin).orElse(null);
            if (fare != null || inventory != null) cabins.add(new CabinManagementResponse(cabin, fare == null ? null : fare.getAmount(), inventory == null ? null : inventory.getTotalSeats(), inventory == null ? null : inventory.getAvailableSeats()));
        }
        return cabins;
    }
}
