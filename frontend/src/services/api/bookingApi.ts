import { apiClient, unwrapData } from './client'
import type { BookingResponse, CreateBookingRequest } from '../../types/domain'

export function createBooking(payload: CreateBookingRequest): Promise<BookingResponse> {
  return unwrapData(apiClient.post('/api/v1/bookings', payload))
}

export function getMyBookings(): Promise<BookingResponse[]> {
  return unwrapData(apiClient.get('/api/v1/bookings/my'))
}

export function getBookingByPnr(pnr: string): Promise<BookingResponse> {
  return unwrapData(apiClient.get(`/api/v1/bookings/${pnr}`))
}

export function cancelBooking(pnr: string): Promise<BookingResponse> {
  return unwrapData(apiClient.patch(`/api/v1/bookings/${pnr}/cancel`))
}
