import { apiClient, unwrapData } from './client'
import type { CabinClass, FlightResponse } from '../../types/domain'

export interface SearchFlightsParams {
  origin: string
  destination: string
  travelDate: string
  passengerCount: number
  cabinClass: CabinClass
}

export function searchFlights(params: SearchFlightsParams): Promise<FlightResponse[]> {
  return unwrapData(
    apiClient.get('/api/v1/flights/search', {
      params,
    }),
  )
}

export function getFlightDetails(flightId: number): Promise<FlightResponse> {
  return unwrapData(apiClient.get(`/api/v1/flights/${flightId}`))
}
