import { apiClient, unwrapData } from './client'
import type {
  AdminAirportResponse,
  AdminFlightResponse,
  BookingResponse,
  CabinClass,
  CabinManagementResponse,
  FlightStatus,
} from '../../types/domain'

export interface AirportRequest {
  code: string
  name: string
  city: string
  country: string
}

export interface CreateFlightPayload {
  airlineName: string
  airlineCode: string
  flightNumber: string
  originAirportId: number
  destinationAirportId: number
  departureTime: string
  arrivalTime: string
  status?: FlightStatus
}

export interface UpdateFlightPayload {
  airlineName: string
  airlineCode: string
  flightNumber: string
  originAirportId: number
  destinationAirportId: number
  departureTime: string
  arrivalTime: string
  status: FlightStatus
}

export interface CabinFarePayload {
  amount: number
}

export interface CabinInventoryPayload {
  totalSeats: number
  availableSeats: number
}

export function createAirport(payload: AirportRequest): Promise<AdminAirportResponse> {
  return unwrapData(apiClient.post('/api/v1/admin/airports', payload))
}

export function getAirports(): Promise<AdminAirportResponse[]> {
  return unwrapData(apiClient.get('/api/v1/admin/airports'))
}

export function updateAirport(id: number, payload: AirportRequest): Promise<AdminAirportResponse> {
  return unwrapData(apiClient.put(`/api/v1/admin/airports/${id}`, payload))
}

export function createFlight(payload: CreateFlightPayload): Promise<AdminFlightResponse> {
  return unwrapData(apiClient.post('/api/v1/admin/flights', payload))
}

export function getFlights(): Promise<AdminFlightResponse[]> {
  return unwrapData(apiClient.get('/api/v1/admin/flights'))
}

export function updateFlight(id: number, payload: UpdateFlightPayload): Promise<AdminFlightResponse> {
  return unwrapData(apiClient.put(`/api/v1/admin/flights/${id}`, payload))
}

export function upsertFare(flightId: number, cabinClass: CabinClass, amount: number): Promise<CabinManagementResponse> {
  return unwrapData(apiClient.put(`/api/v1/admin/flights/${flightId}/fares/${cabinClass}`, { amount }))
}

export function upsertInventory(
  flightId: number,
  cabinClass: CabinClass,
  payload: CabinInventoryPayload,
): Promise<CabinManagementResponse> {
  return unwrapData(apiClient.put(`/api/v1/admin/flights/${flightId}/inventory/${cabinClass}`, payload))
}

export function getCabins(flightId: number): Promise<CabinManagementResponse[]> {
  return unwrapData(apiClient.get(`/api/v1/admin/flights/${flightId}/cabins`))
}

export function getAdminBookings(): Promise<BookingResponse[]> {
  return unwrapData(apiClient.get('/api/v1/admin/bookings'))
}

export function getAdminBooking(pnr: string): Promise<BookingResponse> {
  return unwrapData(apiClient.get(`/api/v1/admin/bookings/${pnr}`))
}

export function cancelAdminBooking(pnr: string): Promise<BookingResponse> {
  return unwrapData(apiClient.patch(`/api/v1/admin/bookings/${pnr}/cancel`))
}
